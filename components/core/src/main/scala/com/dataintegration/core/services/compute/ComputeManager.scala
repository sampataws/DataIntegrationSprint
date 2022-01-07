package com.dataintegration.core.services.compute

import com.dataintegration.core.binders.{Cluster, IntegrationConf, Properties}
import com.dataintegration.core.services.util.{ServiceFrontEnd, ServiceLayer}
import com.dataintegration.core.util.Status
import zio.{UIO, ZIO, ZLayer, ZManaged}

object ComputeManager extends ServiceFrontEnd[Cluster] {

  val live: ZLayer[IntegrationConf with ServiceLayer[Cluster], Nothing, Manager] = {
    for {
      service <- ZIO.service[ServiceLayer[Cluster]]
      integrationConf <- ZIO.service[IntegrationConf]
    } yield Manager(service, integrationConf.getClustersList, integrationConf.getProperties)
  }.toManagedWith(_.stopService).toLayer

  private[compute] case class Manager(
                                       service: ServiceLayer[Cluster],
                                       clusterList: List[Cluster],
                                       properties: Properties) extends ServiceBackEnd {

    override def startService: ZIO[Any, Throwable, List[Cluster]] =
      serviceBuilder(service.onCreate(properties), clusterList, properties.maxParallelism)

    override def getServiceStatus: ZIO[Any, Throwable, List[Cluster]] =
      serviceBuilder(service.getStatus(properties), clusterList, properties.maxParallelism)

    override def stopService: ZIO[Any, Nothing, List[Cluster]] =
      serviceBuilder(service.onDestroy(properties), clusterList.filter(_.status == Status.Running), properties.maxParallelism)
        .fold(e => clusterList, clusterList => clusterList)

    /*** Test ***/
    def startManagedService: ZManaged[Any, Throwable, List[Cluster]] =
      serviceBuilder(
        service.onCreate(properties),
        clusterList,
        properties.maxParallelism)
      .toManagedWith(destroyCluster)


    def destroyCluster(list : List[Cluster]) =
      serviceBuilder(service.onDestroy(properties), list.filter(_.status == Status.Running), properties.maxParallelism)
        .fold(e => clusterList, clusterList => clusterList)

    /*** Test ***/

  }

}
