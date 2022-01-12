package com.dataintegration.gcp.services.storage

import com.dataintegration.core.binders.{FileStoreConfig, IntegrationConf, Properties}
import com.dataintegration.core.services.util.{ServiceLayerV2, ServiceManager}
import com.google.cloud.storage.Storage
import zio.{ZIO, ZLayer}

object StorageManager extends ServiceManager[FileStoreConfig] {

  val live: ZLayer[IntegrationConf with ServiceLayerV2[FileStoreConfig, Storage] with Storage, Nothing, StorageLive] = {
    for {
      client <- ZIO.service[Storage]
      service <- ZIO.service[ServiceLayerV2[FileStoreConfig, Storage]]
      conf <- ZIO.service[IntegrationConf]
    } yield StorageLive(client, service, conf.getFileStore, conf.getProperties)
  }.toLayer

  val liveManaged: ZLayer[IntegrationConf with ServiceLayerV2[FileStoreConfig, Storage] with Storage, Throwable, List[FileStoreConfig]] =
    live >>> Apis.startService.toManagedWith(Apis.stopService).toLayer

  case class StorageLive(
                          client: Storage,
                          service: ServiceLayerV2[FileStoreConfig, Storage],
                          fileStoreList: List[FileStoreConfig],
                          properties: Properties) extends ServiceBackend {

    override def startService: ZIO[Any, Throwable, List[FileStoreConfig]] = serviceBuilder(
      task = service.onCreate(client, properties),
      listOfResources = fileStoreList,
      failureType = FailFast,
      parallelism = properties.maxParallelism)

    override def stopService(fileStoreList: List[FileStoreConfig]): ZIO[Any, Nothing, List[FileStoreConfig]] = serviceBuilder(
      task = service.onDestroy(client, properties),
      listOfResources = fileStoreList,
      failureType = FailSecure,
      parallelism = properties.maxParallelism).orDie

    override def getServiceStatus: ZIO[Any, Throwable, List[FileStoreConfig]] = serviceBuilder(
      task = service.getStatus(client, properties),
      listOfResources = fileStoreList,
      failureType = FailSecure,
      parallelism = properties.maxParallelism)
  }

}
