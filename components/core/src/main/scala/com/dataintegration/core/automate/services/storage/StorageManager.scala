package com.dataintegration.core.automate.services.storage

import com.dataintegration.core.automate.adapter.ServiceContract
import com.dataintegration.core.binders.{FileStoreConfig, IntegrationConf, Properties}
import com.dataintegration.core.services.util.{ServiceLayerAuto, ServiceManager}
import zio.{IsNotIntersection, Tag, Task, ZIO, ZLayer}

class StorageManager[T: Tag : IsNotIntersection] extends ServiceManager[FileStoreConfig] {

  val live: ZLayer[IntegrationConf with ServiceContract[FileStoreConfig, T] with ServiceLayerAuto[FileStoreConfig, T] with T, Nothing, StorageLive] = {
    for {
      client <- ZIO.service[T]
      service <- ZIO.service[ServiceLayerAuto[FileStoreConfig, T]]
      contract <- ZIO.service[ServiceContract[FileStoreConfig, T]]
      conf <- ZIO.service[IntegrationConf]
    } yield StorageLive(client, service, contract, conf.getFileStore, conf.getProperties)
  }.toLayer

  val liveManaged: ZLayer[IntegrationConf with ServiceContract[FileStoreConfig, T] with ServiceLayerAuto[FileStoreConfig, T] with T, Throwable, List[FileStoreConfig]] =
    live >>> Apis.startService.toManagedWith(Apis.stopService).toLayer

  case class StorageLive(
                          client: T,
                          service: ServiceLayerAuto[FileStoreConfig, T],
                          contract: ServiceContract[FileStoreConfig, T],
                          fileStoreList: List[FileStoreConfig],
                          properties: Properties) extends ServiceBackend {

    override def startService: ZIO[Any, Throwable, List[FileStoreConfig]] = serviceBuilder(
      task = service.onCreate(client, contract.createService, properties),
      listOfResources = fileStoreList,
      failureType = FailFast,
      parallelism = properties.maxParallelism)

    override def stopService(fileStoreList: List[FileStoreConfig]): ZIO[Any, Nothing, List[FileStoreConfig]] = serviceBuilder(
      task = service.onCreate(client, contract.destroyService, properties),
      listOfResources = fileStoreList,
      failureType = FailFast,
      parallelism = properties.maxParallelism).orDie

    override def getServiceStatus: ZIO[Any, Throwable, List[FileStoreConfig]] = Task(fileStoreList)

  }

}
