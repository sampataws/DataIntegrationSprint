package com.dataintegration.gcp.services.compute

import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.services.utilv2.{ServiceLayer, ServiceResult}
import com.dataintegration.gcp.services.compute.application.CreateCluster
import com.google.cloud.dataproc.v1.{Cluster, ClusterControllerClient}
import zio.{Task, ULayer, ZLayer}

object ComputeApi extends ServiceLayer[ComputeConfig, Cluster, ClusterControllerClient] {

  override val layer: ULayer[ServiceLayer[ComputeConfig, Cluster, ClusterControllerClient]] = ZLayer.succeed(this)

  override def onCreate(properties: Properties, client: ClusterControllerClient)(data: ComputeConfig): Task[ServiceResult[ComputeConfig, Cluster]] =
    CreateCluster(data, properties, client).execute

  override def onDestroy(properties: Properties, client: ClusterControllerClient)(data: ServiceResult[ComputeConfig, Cluster]): Task[ComputeConfig] =
    ??? //DeleteCluster(data, properties, client).execute

  override def getStatus(properties: Properties, client: ClusterControllerClient)(data: ServiceResult[ComputeConfig, Cluster]): Task[Cluster] = ???
}
