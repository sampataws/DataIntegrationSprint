package com.dataintegration.core.services.job

import com.dataintegration.core.binders.{Cluster, IntegrationConf}
import com.dataintegration.core.services.util.{ServiceLayer, ServiceFrontEnd}
import zio.ZIO

object JobService extends ServiceFrontEnd[Cluster] {

  val live = {
    for {
      integrationConf <- ZIO.service[IntegrationConf]
      clusterService <- ZIO.service[ServiceLayer[Cluster]]
    } yield JobSubmit(integrationConf.getClustersList, clusterService)
  }.toLayer

  case class JobSubmit(clusterList: List[Cluster], clusterService: ServiceLayer[Cluster]) extends ServiceBackEnd {
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
