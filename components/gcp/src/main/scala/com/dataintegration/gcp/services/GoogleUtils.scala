package com.dataintegration.gcp.services

import java.nio.file.{Files, Paths}

import com.dataintegration.core.binders.{ComputeConfig, FileStoreConfig, JobConfig}
import com.dataintegration.core.util.{ApplicationLogger, ApplicationUtils, Status}
import com.google.cloud.dataproc.v1._
import com.google.cloud.storage.{BlobInfo, CopyWriter, Storage}
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Duration

import scala.jdk.CollectionConverters._
//import scala.jdk.CollectionConverters._

object GoogleUtils extends ApplicationLogger {

  val className: String = this.getClass.getSimpleName.stripSuffix("$")

  def createDataprocCluster(data: ComputeConfig, client: ClusterControllerClient): ComputeConfig = {

    val diskConf = (diskVolume: Int) => DiskConfig.newBuilder().setBootDiskType("pd-ssd").setBootDiskSizeGb(diskVolume)

    val createInstanceGroup = (imageName: String, numInstances: Int, diskVolume: Int) =>
      InstanceGroupConfig.newBuilder()
        .setMachineTypeUri(imageName)
        .setNumInstances(numInstances)
        //.setImageUri(data.imageVersion)
        .setDiskConfig(diskConf(diskVolume))
        //.setPreemptibility(Preemptibility.PREEMPTIBLE)
        .build()

    val masterInstanceGroup = createInstanceGroup(data.masterMachineTypeUri, data.masterNumInstance, data.masterBootDiskSizeGB)
    val workedInstanceGroup = createInstanceGroup(data.workerMachineTypeUri, data.workerNumInstance, data.workerBootDiskSizeGB)

    val clusterConfig = ClusterConfig.newBuilder()
      .setMasterConfig(masterInstanceGroup)
      .setWorkerConfig(workedInstanceGroup)
      .setSoftwareConfig(SoftwareConfig.newBuilder().setImageVersion(data.imageVersion))
      .setConfigBucket(data.bucketName)
      .setLifecycleConfig(LifecycleConfig.newBuilder().setIdleDeleteTtl(Duration.newBuilder().setSeconds(data.idleDeletionDurationSec)))
      .build()

    val dataprocCluster = Cluster.newBuilder().setClusterName(data.clusterName).setConfig(clusterConfig).build()
    val request = client.createClusterAsync(data.project, data.region, dataprocCluster)
    val response = request.get()
    logger.info(s"[$className] Cluster ${response.getClusterName} started with Api response ${response.toString}")

    data.copy(status = Status.Running)
  }

  // Later on first check if cluster exists via status apis.
  def deleteCluster(config: ComputeConfig, client: ClusterControllerClient): ComputeConfig = {
    val response = client.deleteClusterAsync(config.project, config.region, config.clusterName).get()
    logger.info(s"[$className] Cluster ${config.getName} deleted with Api response ${response.toString}")
    config.copy(status = Status.Success)
  }

  def listOfRunningClusters(config: ComputeConfig, client: ClusterControllerClient): Seq[String] = {
    val clusterList = client.listClusters(config.project, config.region)
    clusterList.iterateAll().asScala.map(cluster => cluster.getClusterName).toSeq
  }

  /**
   * Note :-
   *    path should'nt start with / and should be the name of the file
   */
  def copyFiles(storage: Storage, data: FileStoreConfig): FileStoreConfig = {
    data.sourceBucket.trim.toLowerCase match {
      case "local" => copyLocalToGCS
      case _ => copyGCStoGCS
    }

    def getFullPath(bucket: String, path: String) =
      ApplicationUtils.cleanForwardSlash(bucket + "/" + path)

    def copyLocalToGCS: Boolean = {
      val blobInfo = BlobInfo.newBuilder(data.targetBucket.get, data.targetPath.get).build()
      val response = storage.create(blobInfo, Files.readAllBytes(Paths.get(data.sourcePath)))
      logger.info(s"[$className] Files Copied from ${data.sourcePath} to " + getFullPath(data.targetBucket.get, data.targetPath.get) +
        s" with Api response ${response.toString}")
      true
    }

    def copyGCStoGCS: Boolean = {
      val blobList = storage.list(data.sourceBucket, Storage.BlobListOption.prefix(data.sourcePath))
      blobList.iterateAll().asScala.foreach { blob =>
        val res: CopyWriter = blob.copyTo(data.targetBucket.get, data.targetPath.get)

        logger.info(s"[$className] Files Copied from ${data.sourceBucket}" + getFullPath(data.targetBucket.get, data.targetPath.get) +
          s" with Api response ${res.toString}")
      }
      true
    }

    data.copy(status = Status.Success)
  }

  def deleteFiles(storage: Storage, data: FileStoreConfig): FileStoreConfig = {
    val blobList = storage.list(data.sourceBucket, Storage.BlobListOption.prefix(data.sourcePath))
    blobList.iterateAll().asScala.foreach { file =>
      val response = file.delete()
      logger.info(s"[$className] Deleted Files " + ApplicationUtils.cleanForwardSlash(file.getBucket + "/" + file.getName) +
        s" with Api response ${response.toString}")
    }
    data.copy(status = Status.Success)
  }

  /**
   * everything has to be in gs
   */
  def submitSparkJob(client: JobControllerClient, jobConfig: JobConfig): JobConfig = {

    val jobPlacement = JobPlacement.newBuilder().setClusterName(jobConfig.compute.clusterName).build()

    // Has to be here as service id gets generated here
    val sparkProperties =
      if (jobConfig.sparkConf.contains("spark.app.name")) jobConfig.sparkConf
      else jobConfig.sparkConf ++ Map("spark.app.name" -> s"${jobConfig.name}-${jobConfig.serviceId}")

    val sparkJob = SparkJob.newBuilder()
      .setMainClass(jobConfig.className)
      .addAllJarFileUris(jobConfig.libraryList.asJava)
      .addAllArgs(jobConfig.programArguments.asJava)
      .putAllProperties(sparkProperties.asJava)
      .build()

    val jobBuilder = Job.newBuilder().setPlacement(jobPlacement).setSparkJob(sparkJob).build()

    @scala.annotation.tailrec
    def poolStatus(jobId: String): JobConfig = {

      val status = client.getJob(jobConfig.compute.project, jobConfig.compute.region,jobId).getStatus.getState.toString.toUpperCase

      def loggingText = s"[$className] ${jobConfig.name}:$jobId status :- $status"

      status match {
        case "DONE" =>
          logger.info(loggingText)
          jobConfig.copy(status = Status.Success)
        case "CANCELLED" | "ERROR" =>
          logger.error(loggingText)
          throw new Exception(loggingText)
        case "PENDING" | "RUNNING" =>
          logger.info(loggingText)
          Thread.sleep(1000 * 10)
          poolStatus(jobId)
        case _ =>
          logger.info(status)
          logger.info(loggingText)
          Thread.sleep(1000 * 10)
          poolStatus(jobId)

      }
    }
    val response = client.submitJob(jobConfig.compute.project, jobConfig.compute.region, jobBuilder)
    val jobId = response.getReference.getJobId
    logger.info(s"Job submitted as $jobId")
    poolStatus(jobId)
  }

}
