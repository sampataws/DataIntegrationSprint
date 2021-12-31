package com.dataintegration.core.services.job

import com.dataintegration.core.binders.{Cluster, IntegrationConf, Properties}
import com.dataintegration.core.services.util.{ServiceFrontEnd, ServiceLayer}
import zio.{ZIO, ZLayer}

object JobManager extends ServiceFrontEnd[Cluster] {

  val live: ZLayer[ServiceLayer[Cluster] with IntegrationConf, Nothing, Manager] = {
    for {
      clusterService <- ZIO.service[ServiceLayer[Cluster]]
      integrationConf <- ZIO.service[IntegrationConf]
    } yield Manager(clusterService, integrationConf.getClustersList, integrationConf.getProperties)
  }.toLayer

  case class Manager(
                      clusterService: ServiceLayer[Cluster],
                      clusterList: List[Cluster],
                      properties: Properties) extends ServiceBackEnd {

    override def onCreate: ZIO[Any, Throwable, List[Cluster]] = for {
      output <- ZIO.foreachPar(clusterList)(clusterService.onCreate).withParallelism(5)
    } yield output

    override def onDestroy: ZIO[Any, Throwable, List[Cluster]] = for {
      output <- ZIO.foreachPar(clusterList)(clusterService.onDestroy).withParallelism(5)
    } yield output

    override def getStatus: ZIO[Any, Throwable, List[Cluster]] = for {
      output <- ZIO.foreachPar(clusterList)(clusterService.getStatus).withParallelism(5)
    } yield output
  }

}
