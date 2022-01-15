package com.dataintegration.gcp.services.compute

import com.dataintegration.core.binders.{ComputeConfig, IntegrationConf, Properties}
import com.dataintegration.core.services.util.{ServiceLayer, ServiceManager}
import com.google.cloud.dataproc.v1.ClusterControllerClient
import zio.{ZIO, ZLayer}

object ComputeManager extends ServiceManager[ComputeConfig] {

  val live: ZLayer[IntegrationConf with ServiceLayer[ComputeConfig, ClusterControllerClient] with ClusterControllerClient, Nothing, ComputeLive] = {
    for {
      client <- ZIO.service[ClusterControllerClient]
      service <- ZIO.service[ServiceLayer[ComputeConfig, ClusterControllerClient]]
      conf <- ZIO.service[IntegrationConf]
    } yield ComputeLive(client, service, conf.getClustersList, conf.getProperties)
  }.toLayer

  val liveManaged: ZLayer[IntegrationConf with ServiceLayer[ComputeConfig, ClusterControllerClient] with ClusterControllerClient, Throwable, List[ComputeConfig]] =
    live >>> Apis.startService.toManagedWith(Apis.stopService).toLayer

  case class ComputeLive(
                          client: ClusterControllerClient,
                          service: ServiceLayer[ComputeConfig, ClusterControllerClient],
                          clustersList: List[ComputeConfig],
                          properties: Properties) extends ServiceBackend {

    override def startService: ZIO[Any, Throwable, List[ComputeConfig]] = serviceBuilder(
      task = service.onCreate(client, properties),
      listOfResources = clustersList,
      failureType = FailSafe,
      parallelism = properties.maxParallelism)

    override def stopService(clustersList: List[ComputeConfig]): ZIO[Any, Nothing, List[ComputeConfig]] = serviceBuilder(
      task = service.onDestroy(client, properties),
      listOfResources = clustersList,
      failureType = FailSafe,
      parallelism = properties.maxParallelism).orDie

    override def getServiceStatus: ZIO[Any, Throwable, List[ComputeConfig]] = serviceBuilder(
      task = service.getStatus(client, properties),
      listOfResources = clustersList,
      failureType = FailSecure,
      parallelism = properties.maxParallelism)

  }

}
