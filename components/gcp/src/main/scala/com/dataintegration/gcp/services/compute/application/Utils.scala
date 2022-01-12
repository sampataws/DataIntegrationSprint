package com.dataintegration.gcp.services.compute.application

import com.dataintegration.core.binders.ComputeConfig
import com.google.api.gax.longrunning.OperationFuture
import com.google.cloud.dataproc.v1.InstanceGroupConfig.Preemptibility
import com.google.cloud.dataproc.v1._
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.{Duration, Empty}

object Utils {

  def createDataprocCluster(data: ComputeConfig, clusterControllerClient: ClusterControllerClient): Cluster = {

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
    val request = clusterControllerClient.createClusterAsync(data.project, data.region, dataprocCluster)
    request.get()
  }

  def deleteCluster(cluster: Cluster, clusterControllerClient: ClusterControllerClient): OperationFuture[Empty, ClusterOperationMetadata] = {
    val endpoint = cluster.getField(FieldDescriptor.Type.STRING("endpoint")).toString
    clusterControllerClient.deleteClusterAsync(cluster.getProjectId, endpoint, cluster.getClusterName)
  }

  // Later on first check if cluster exists via status apis.
  def deleteCluster(config : ComputeConfig, cluster: Cluster, client : ClusterControllerClient): OperationFuture[Empty, ClusterOperationMetadata] =
    client.deleteClusterAsync(config.project, config.endpoint, cluster.getClusterName)

}
