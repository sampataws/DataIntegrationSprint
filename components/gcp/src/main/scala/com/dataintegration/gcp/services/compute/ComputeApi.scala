package com.dataintegration.gcp.services.compute

import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.services.util.ServiceResult
import com.dataintegration.core.services.utilv2.ServiceLayer
import com.dataintegration.gcp.services.compute.application.CreateCluster
import com.google.cloud.dataproc.v1.{ClusterControllerClient, Cluster => Dataproc}
import zio.{Task, ULayer, ZLayer}

object ComputeApi extends ServiceLayer[ComputeConfig, Dataproc, ClusterControllerClient] {
  override val layer: ULayer[ServiceLayer[ComputeConfig, Dataproc, ClusterControllerClient]] = ZLayer.succeed(this)

  override def onCreate(properties: Properties, client: ClusterControllerClient)(data: ComputeConfig): Task[ServiceResult[ComputeConfig, Dataproc]] =
    CreateCluster(data, properties, client)

  override def onDestroy(properties: Properties, client: ClusterControllerClient)(data: ServiceResult[ComputeConfig, Dataproc]): Task[ComputeConfig] = ???

  override def getStatus(properties: Properties, client: ClusterControllerClient)(data: ServiceResult[ComputeConfig, Dataproc]): Task[Dataproc] = ???
}
