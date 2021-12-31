package com.dataintegration.core.services.job

import com.dataintegration.core.binders.{Cluster, IntegrationConf}
import com.dataintegration.core.services.util.{Service, ServiceFrontLayer}
import zio.ZIO

object JobService extends ServiceFrontLayer[Cluster] {

  val live = {
    for {
      integrationConf <- ZIO.service[IntegrationConf]
      clusterService <- ZIO.service[Service[Cluster]]
    } yield JobSubmit(integrationConf.getClustersList, clusterService)
  }.toLayer

  case class JobSubmit(clusterList: List[Cluster], clusterService: Service[Cluster]) extends ServiceBackLayer {
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
