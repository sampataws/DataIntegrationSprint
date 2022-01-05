package com.dataintegration.core.services.compute

import com.dataintegration.core.binders.{Cluster, IntegrationConf, Properties}
import com.dataintegration.core.services.util.{ServiceFrontEnd, ServiceLayer}
import zio.{Task, UIO, ZIO, ZLayer}

object ComputeManager extends ServiceFrontEnd[Cluster] {

  val live: ZLayer[IntegrationConf with ServiceLayer[Cluster], Nothing, Manager] = {
    for {
      service <- ZIO.service[ServiceLayer[Cluster]]
      integrationConf <- ZIO.service[IntegrationConf]
    } yield Manager(service, integrationConf.getClustersList, integrationConf.getProperties)
  }.toManagedWith(_.shutdown).toLayer

  private[compute] case class Manager(
                                       service: ServiceLayer[Cluster],
                                       clusterList: List[Cluster],
                                       properties: Properties) extends ServiceBackEnd {

    def builder(task: Cluster => Task[Cluster]): ZIO[Any, Throwable, List[Cluster]] =
      ZIO.foreachPar(clusterList)(task).withParallelism(properties.maxClusterParallelism)

    override def startService: ZIO[Any, Throwable, List[Cluster]] = builder(service.onCreate(properties))

    override def stopService: ZIO[Any, Throwable, List[Cluster]] = builder(service.onDestroy(properties))

    override def getServiceStatus: ZIO[Any, Throwable, List[Cluster]] = builder(service.getStatus(properties))

    def shutdown : UIO[Unit] = ZIO.succeed("")
  }

}
