package com.dataintegration.core.services.storage

import com.dataintegration.core.binders.{FileStore, IntegrationConf, Properties}
import com.dataintegration.core.services.util.{ServiceLayer, ServiceManager}
import zio.{ZIO, ZLayer}

object StorageManager extends ServiceManager[FileStore] {

  val live: ZLayer[IntegrationConf with ServiceLayer[FileStore], Nothing, StorageManagerLive] = {
    for {
      service <- ZIO.service[ServiceLayer[FileStore]]
      conf <- ZIO.service[IntegrationConf]
    } yield StorageManagerLive(service, conf.getFileStore, conf.getProperties)
  }.toLayer

  val liveManaged: ZLayer[IntegrationConf with ServiceLayer[FileStore], Throwable, List[FileStore]] =
    live >>> Apis.startService.toManagedWith(Apis.stopService).toLayer

  case class StorageManagerLive(
                                 fileStore: ServiceLayer[FileStore],
                                 fileStoreList: List[FileStore],
                                 properties: Properties) extends ServiceBackend {

    override def startService: ZIO[Any, Throwable, List[FileStore]] = serviceBuilder(
      task = fileStore.onCreate(properties),
      listOfResources = fileStoreList,
      failureType = FailFast,
      parallelism = properties.maxParallelism)

    override def stopService(upServices: List[FileStore]): ZIO[Any, Nothing, List[FileStore]] = serviceBuilder(
      task = fileStore.onDestroy(properties),
      listOfResources = fileStoreList,
      failureType = FailFast,
      parallelism = properties.maxParallelism).orDie

    override def getServiceStatus: ZIO[Any, Throwable, List[FileStore]] = serviceBuilder(
      task = fileStore.getStatus(properties),
      listOfResources = fileStoreList,
      failureType = FailFast,
      parallelism = properties.maxParallelism)
  }

}
