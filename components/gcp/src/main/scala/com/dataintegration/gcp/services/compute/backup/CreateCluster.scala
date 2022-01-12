package com.dataintegration.gcp.services.compute.backup

import com.dataintegration.core.binders.{Cluster, Properties}
import com.dataintegration.core.services.util.{ServiceApi, ServiceResult}
import com.dataintegration.core.util.Status
import com.google.cloud.dataproc.v1.InstanceGroupConfig.Preemptibility
import com.google.cloud.dataproc.v1._
import com.google.protobuf.Duration
import zio.Task

case class CreateCluster(data: Cluster, properties: Properties) extends ServiceApi[ServiceResult[Cluster, DataprocCluster]] {
  /**
   * Inject deps of clusterControllerClient
   *  on create step -> on destroy or get status
   *
   */

  override def preJob(): Task[Unit] = ??? //Logging.atStart(data)
  override def mainJob: Task[ServiceResult[Cluster, DataprocCluster]] = ???
  override def postJob(serviceResult: ServiceResult[Cluster, DataprocCluster]): Task[Unit] = ??? //Logging.atStop(serviceResult)
  override def onSuccess: () => ServiceResult[Cluster, DataprocCluster] = () => ServiceResult(data.onSuccess(Status.Running),???)
  override def onFailure: Throwable => ServiceResult[Cluster, DataprocCluster] = data.onFailure(Status.Failed)
  override def retries: Int = properties.maxRetries

  private def spawnDataprocCluster(): Unit = {
    // should be singleton
    val clusterControllerSettings = ClusterControllerSettings.newBuilder().setEndpoint(data.endpoint).build()
    val clusterControllerClient = ClusterControllerClient.create(clusterControllerSettings)

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
      .setLifecycleConfig(LifecycleConfig.newBuilder().setIdleDeleteTtl(Duration.newBuilder().setSeconds(data.idleDeletionDurationSec)))
      .build()

    val dataprocCluster = DataprocCluster.newBuilder().setClusterName(data.clusterName).setConfig(clusterConfig).build()
    val request = clusterControllerClient.createClusterAsync(data.project, data.region, dataprocCluster)
    val response = request.get()
    response.getStatus.getState.getDescriptorForType

    //clusterControllerClient.deleteClusterAsync(data.project, data.region, data.clusterName)


  }
}
