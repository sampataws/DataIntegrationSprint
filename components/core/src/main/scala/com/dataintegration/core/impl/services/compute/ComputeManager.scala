package com.dataintegration.core.impl.services.compute

import com.dataintegration.core.impl.adapter.{ServiceContract, ServiceLayerGenericImpl}
import com.dataintegration.core.binders.{ComputeConfig, IntegrationConf, Properties}
import com.dataintegration.core.services.util.ServiceManager
import zio.{IsNotIntersection, Tag, Task, ZIO, ZLayer}

class ComputeManager[T: Tag : IsNotIntersection] extends ServiceManager[ComputeConfig] {

  val live: ZLayer[IntegrationConf with ServiceContract[ComputeConfig, T] with ServiceLayerGenericImpl[ComputeConfig, T] with T, Nothing, ComputeLive] = {
    for {
      client <- ZIO.service[T]
      service <- ZIO.service[ServiceLayerGenericImpl[ComputeConfig, T]]
      contract <- ZIO.service[ServiceContract[ComputeConfig, T]]
      conf <- ZIO.service[IntegrationConf]
    } yield ComputeLive(client, service, contract, conf.getClustersList, conf.getProperties)
  }.toLayer

  val liveManaged: ZLayer[IntegrationConf with ServiceContract[ComputeConfig, T] with ServiceLayerGenericImpl[ComputeConfig, T] with T, Throwable, List[ComputeConfig]] =
    live >>> Apis.startService.toManagedWith(Apis.stopService).toLayer

  case class ComputeLive(
                          client: T,
                          service: ServiceLayerGenericImpl[ComputeConfig, T],
                          contract: ServiceContract[ComputeConfig, T],
                          clustersList: List[ComputeConfig],
                          properties: Properties) extends ServiceBackend {

    override def startService: ZIO[Any, Throwable, List[ComputeConfig]] = serviceBuilder(
      task = service.onCreate(client, contract.createService, properties),
      listOfResources = clustersList,
      failureType = FailSafe,
      parallelism = properties.maxParallelism)

    override def stopService(clustersList: List[ComputeConfig]): ZIO[Any, Nothing, List[ComputeConfig]] = serviceBuilder(
      task = service.onDestroy(client, contract.destroyService, properties),
      listOfResources = clustersList,
      failureType = FailSafe,
      parallelism = properties.maxParallelism).orDie

    override def getServiceStatus: ZIO[Any, Throwable, List[ComputeConfig]] = Task(clustersList)
  }

}