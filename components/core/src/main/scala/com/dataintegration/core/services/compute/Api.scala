package com.dataintegration.core.services.compute

import com.dataintegration.core.binders.Cluster
import com.dataintegration.core.services.util.ServiceLayer
import zio.{ZIO, ZLayer}

trait Api extends ServiceLayer[Cluster] {


  override def onCreate: ZIO[BackendLayer, Nothing, Cluster] =
    ZIO.serviceWith[BackendLayer](_.onCreate)

  override def onDelete: ZIO[BackendLayer, Nothing, Cluster] =
    ZIO.serviceWith[BackendLayer](_.onCreate)

  override def getStatus: ZIO[BackendLayer, Nothing, Cluster] =
    ZIO.serviceWith[BackendLayer](_.onCreate)


  override val live: ZLayer[Cluster, Nothing, BackendLayer] = {
    for {
      cluster <- ZIO.service[Cluster]
    } yield createFunctionalDependencies(cluster)
  }.toLayer
}
