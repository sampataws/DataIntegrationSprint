package com.dataintegration.core.services.compute

import com.dataintegration.core.binders.{Cluster, IntegrationConf, Properties}
import com.dataintegration.core.services.util.{ServiceFrontEnd, ServiceLayerV2}
import zio.{Task, ZIO, ZLayer}

object ComputeManager extends ServiceFrontEnd[Cluster] {

  val live: ZLayer[IntegrationConf with ServiceLayerV2[Cluster], Nothing, Manager] = {
    for {
      service <- ZIO.service[ServiceLayerV2[Cluster]]
      integrationConf <- ZIO.service[IntegrationConf]
    } yield Manager(service, integrationConf.getClustersList, integrationConf.getProperties)
  }.toLayer

  private[compute] case class Manager(
                                       service: ServiceLayerV2[Cluster],
                                       clusterList: List[Cluster],
                                       properties: Properties) extends ServiceBackEnd {

    def builder(task: Cluster => Task[Cluster]): ZIO[Any, Throwable, List[Cluster]] =
      ZIO.foreachPar(clusterList)(task).withParallelism(properties.maxClusterParallelism)

    override def onCreate: ZIO[Any, Throwable, List[Cluster]] = builder(service.onCreate(properties))

    override def onDestroy: ZIO[Any, Throwable, List[Cluster]] = builder(service.onDestroy(properties))

    override def getStatus: ZIO[Any, Throwable, List[Cluster]] = builder(service.getStatus(properties))
  }

}
