package com.dataintegration.gcp.services.compute.backup

import com.dataintegration.core.binders.{Cluster, Properties}
import com.dataintegration.core.services.util.ServiceResult
import com.dataintegration.core.services.utilv2.ServiceLayer
import zio.{Task, ULayer, ZLayer}

object GoogleComputeApi extends ServiceLayer[Cluster, DataprocCluster] {
  override val layer: ULayer[ServiceLayer[Cluster, DataprocCluster]] = ZLayer.succeed(this)
  override def onCreate(properties: Properties)(data: Cluster): Task[ServiceResult[Cluster, DataprocCluster]] = ???
  override def onDestroy[R](properties: Properties)(data: ServiceResult[Cluster, DataprocCluster]): Task[R] = ???
  override def getStatus[R](properties: Properties)(data: ServiceResult[Cluster, DataprocCluster]): Task[R] = ???
}
