package com.dataintegration.gcp.services.compute

import com.dataintegration.core.binders.{Cluster, Properties}
import com.dataintegration.core.services.util.ServiceLayer
import zio.{Task, ULayer, ZLayer}

object GoogleComputeApi extends ServiceLayer[Cluster] {
  override val layer: ULayer[ServiceLayer[Cluster]] = ZLayer.succeed(this)
  override def onCreate(properties: Properties)(data: Cluster): Task[Cluster] = ???
  override def onDestroy(properties: Properties)(data: Cluster): Task[Cluster] = ???
  override def getStatus(properties: Properties)(data: Cluster): Task[Cluster] = ???
}
