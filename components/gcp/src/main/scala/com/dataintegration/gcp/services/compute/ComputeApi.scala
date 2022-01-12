package com.dataintegration.gcp.services.compute

import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.services.util.ServiceLayerV2
import com.dataintegration.gcp.services.compute.applicationv2.{CreateCluster, DeleteCluster}
import com.google.cloud.dataproc.v1.ClusterControllerClient
import zio.{Task, ULayer, ZLayer}

class ComputeApi extends ServiceLayerV2[ComputeConfig, ClusterControllerClient] {
  override val layer: ULayer[ServiceLayerV2[ComputeConfig, ClusterControllerClient]] = ZLayer.succeed(this)

  override def onCreate(client: ClusterControllerClient, properties: Properties)(data: ComputeConfig): Task[ComputeConfig] =
    CreateCluster(client, data, properties).execute

  override def onDestroy(client: ClusterControllerClient, properties: Properties)(data: ComputeConfig): Task[ComputeConfig] =
    DeleteCluster(client, data, properties).execute

  override def getStatus(client: ClusterControllerClient, properties: Properties)(data: ComputeConfig): Task[ComputeConfig] =
    Task(data)
}
