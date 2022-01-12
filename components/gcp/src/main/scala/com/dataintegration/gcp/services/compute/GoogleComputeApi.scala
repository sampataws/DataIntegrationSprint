package com.dataintegration.gcp.services.compute

import com.dataintegration.core.binders.{Cluster, Properties}
import com.dataintegration.core.services.util.{ServiceLayerV2, ServiceResult}
import com.google.cloud.dataproc.v1.{Cluster => DataprocCluster}
import zio.{Task, ULayer, ZLayer}

object GoogleComputeApi extends ServiceLayerV2[Cluster, DataprocCluster] {
  override val layer: ULayer[ServiceLayerV2[Cluster, DataprocCluster]] = ZLayer.succeed(this)
  override def onCreate(properties: Properties)(data: Cluster): Task[ServiceResult[Cluster, DataprocCluster]] = ???
  override def onDestroy[R](properties: Properties)(data: ServiceResult[Cluster, DataprocCluster]): Task[R] = ???
  override def getStatus[R](properties: Properties)(data: ServiceResult[Cluster, DataprocCluster]): Task[R] = ???
}
