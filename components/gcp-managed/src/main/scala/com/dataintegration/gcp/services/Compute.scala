package com.dataintegration.gcp.services

import com.dataintegration.core.binders.{ComputeConfig, IntegrationConf, Properties}
import com.dataintegration.core.impl.adapter.contracts.ComputeContract
import com.dataintegration.core.services.log.audit.DatabaseService
import com.dataintegration.core.util.Status
import com.google.cloud.dataproc.v1._
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Duration
import zio.ZLayer

import scala.jdk.CollectionConverters._
//import scala.jdk.CollectionConverters._


object Compute extends ComputeContract[ClusterControllerClient] {

  val className: String = this.getClass.getSimpleName.stripSuffix("$")

  override def createClient(properties: Properties): ClusterControllerClient = {
    // todo config endpoint
    val clusterControllerSettings = ClusterControllerSettings.newBuilder()
      .setEndpoint("us-central1-dataproc.googleapis.com:443").build()

    val clusterControllerClient = ClusterControllerClient.create(clusterControllerSettings)
    logger.info("Cluster client initiated")
    clusterControllerClient
  }

  override def destroyClient(client: ClusterControllerClient): Unit = {
    client.close()
    logger.info("Cluster client destroyed")
  }

  override def createService(client: ClusterControllerClient, data: ComputeConfig): ComputeConfig = {
    val diskConf = (diskVolume: Int) => DiskConfig.newBuilder().setBootDiskType("pd-ssd").setBootDiskSizeGb(diskVolume)

    val createInstanceGroup = (imageName: String, numInstances: Int, diskVolume: Int) =>
      InstanceGroupConfig.newBuilder()
        .setMachineTypeUri(imageName)
        .setNumInstances(numInstances)
        .setImageUri(data.imageVersion)
        .setDiskConfig(diskConf(diskVolume))
        //.setPreemptibility(Preemptibility.PREEMPTIBLE)
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
    val response = request.get()
    logger.info(s"[$className] Cluster ${response.getClusterName} started with Api response ${response.toString}")

    data.copy(status = Status.Running)
  }

  override def destroyService(client: ClusterControllerClient, data: ComputeConfig): ComputeConfig = {
    val runningCluster = listOfRunningClusters(client, data)
    if (runningCluster.contains(data.clusterName)) {
      val response = client.deleteClusterAsync(data.project, data.region, data.clusterName)
      logger.info(s"[$className] Cluster ${data.getName} deleted with Api response ${response.toString}")
      data.copy(status = Status.Success)
    } else {
      logger.info(s"[$className] Cluster :- not in running state")
      data.copy(status = Status.Success)
    }
  }

  def listOfRunningClusters(client: ClusterControllerClient, data: ComputeConfig): Seq[String] = {
    val clusterList = client.listClusters(data.project, data.region)
    clusterList.iterateAll().asScala.map(cluster => cluster.getClusterName).toSeq
  }

  override def partialDependencies: ZLayer[Any with IntegrationConf with DatabaseService.AuditTableApi, Throwable, List[ComputeConfig]] =
    (contractLive ++ serviceApiLive ++ clientLive) >>> serviceManager.liveManaged
}
