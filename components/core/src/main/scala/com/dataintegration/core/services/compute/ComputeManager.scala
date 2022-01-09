package com.dataintegration.core.services.compute

import com.dataintegration.core.binders.{Cluster, IntegrationConf, Properties}
import com.dataintegration.core.services.util.{ServiceLayer, ServiceManager}
import zio.{ZIO, ZLayer}

object ComputeManager extends ServiceManager[Cluster] {

  val live: ZLayer[IntegrationConf with ServiceLayer[Cluster], Throwable, ComputeManagerLive] = {
    for {
      service <- ZIO.service[ServiceLayer[Cluster]]
      conf <- ZIO.service[IntegrationConf]
    } yield ComputeManagerLive(service, conf.getClustersList, conf.getProperties)
  }.toLayer

  val liveManaged: ZLayer[IntegrationConf with ServiceLayer[Cluster], Throwable, List[Cluster]] =
    live >>> Apis.startService.toManagedWith(Apis.stopService).toLayer

  case class ComputeManagerLive(
                                 clusterService: ServiceLayer[Cluster],
                                 clusterList: List[Cluster],
                                 properties: Properties) extends ServiceBackend {

    override def startService: ZIO[Any, Throwable, List[Cluster]] = serviceBuilder(
      task = clusterService.onCreate(properties),
      listOfResources = clusterList,
      failureType = FailSafe,
      parallelism = properties.maxParallelism)

    override def stopService(upServices: List[Cluster]): ZIO[Any, Nothing, List[Cluster]] = serviceBuilder(
      task = clusterService.onDestroy(properties),
      listOfResources = upServices,
      failureType = FailSafe,
      parallelism = properties.maxParallelism).orDie

    override def getServiceStatus: ZIO[Any, Throwable, List[Cluster]] = serviceBuilder(
      task = clusterService.getStatus(properties),
      listOfResources = clusterList,
      failureType = FailSafe,
      parallelism = properties.maxParallelism)

  }

}
