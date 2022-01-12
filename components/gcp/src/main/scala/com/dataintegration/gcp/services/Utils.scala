package com.dataintegration.gcp.services

import java.nio.file.{Files, Paths}

import com.dataintegration.core.binders.{ComputeConfig, FileStoreConfig}
import com.dataintegration.core.util.Status
import com.google.api.gax.longrunning.OperationFuture
import com.google.cloud.dataproc.v1.InstanceGroupConfig.Preemptibility
import com.google.cloud.dataproc.v1._
import com.google.cloud.storage.{BlobInfo, Storage}
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.{Duration, Empty}

object Utils {

  def createDataprocCluster(data: ComputeConfig, client: ClusterControllerClient): Cluster = {

    val diskConf = (diskVolume: Int) => DiskConfig.newBuilder().setBootDiskType("pd-ssd").setBootDiskSizeGb(diskVolume)

    val createInstanceGroup = (imageName: String, numInstances: Int, diskVolume: Int) =>
      InstanceGroupConfig.newBuilder()
        .setMachineTypeUri(imageName)
        .setNumInstances(numInstances)
        .setImageUri(data.imageVersion)
        .setDiskConfig(diskConf(diskVolume))
        .setPreemptibility(Preemptibility.PREEMPTIBLE)
        .build()

    val masterInstanceGroup = createInstanceGroup(data.masterMachineTypeUri, data.masterNumInstance, data.masterBootDiskSizeGB)
    val workedInstanceGroup = createInstanceGroup(data.workerMachineTypeUri, data.workerNumInstance, data.workerBootDiskSizeGB)

    val clusterConfig = ClusterConfig.newBuilder()
      .setMasterConfig(masterInstanceGroup)
      .setWorkerConfig(workedInstanceGroup)
      .setConfigBucket(data.bucketName)
      .setField(FieldDescriptor.Type.STRING("endpoint"), data.endpoint)
      .setLifecycleConfig(LifecycleConfig.newBuilder().setIdleDeleteTtl(Duration.newBuilder().setSeconds(data.idleDeletionDurationSec)))
      .build()

    val dataprocCluster = Cluster.newBuilder().setClusterName(data.clusterName).setConfig(clusterConfig).build()
    val request = client.createClusterAsync(data.project, data.region, dataprocCluster)
    request.get()
  }

  // Later on first check if cluster exists via status apis.
  def deleteCluster(config: ComputeConfig, client: ClusterControllerClient): OperationFuture[Empty, ClusterOperationMetadata] =
    client.deleteClusterAsync(config.project, config.region, config.clusterName)

  def listOfRunningClusters(config: ComputeConfig, client: ClusterControllerClient): Seq[String] = {
    val clusterList = client.listClusters(config.project, config.region)
    clusterList.iterateAll().asScala.map(cluster => cluster.getClusterName).toSeq
  }

  def copyFiles(storage: Storage, data: FileStoreConfig): FileStoreConfig = {
    data.sourceBucket.trim.toLowerCase match {
      case "local" => copyLocalToGCS
      case _ => copyGCStoGCS
    }

    def copyLocalToGCS: Boolean = {
      val blobInfo = BlobInfo.newBuilder(data.targetBucket.get, data.targetPath.get).build()
      storage.create(blobInfo, Files.readAllBytes(Paths.get(data.targetPath.get)))
      true
    }

    def copyGCStoGCS: Boolean = {
      val blobList = storage.list(data.sourceBucket, Storage.BlobListOption.prefix(data.sourcePath))
      blobList.iterateAll().asScala.map { blob =>
        blob.copyTo(data.targetBucket.get, data.targetPath.get)
      }
      true
    }

    data.copy(status = Status.Success)
  }

  def deleteFiles(storage: Storage, data: FileStoreConfig): FileStoreConfig = {
    val blobList = storage.list(data.sourceBucket, Storage.BlobListOption.prefix(data.sourcePath))
    blobList.iterateAll().asScala.map(_.delete())
    data.copy(status = Status.Success)
  }

}
