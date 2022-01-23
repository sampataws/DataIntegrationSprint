package com.dataintegration.core.impl.services.storage

import com.dataintegration.core.binders.{FileStoreConfig, IntegrationConf, Properties}
import com.dataintegration.core.impl.adapter.ServiceLayerGenericImpl
import com.dataintegration.core.impl.adapter.contracts.ServiceContract
import com.dataintegration.core.services.log.audit.{DatabaseServiceV2 => DatabaseService}
import com.dataintegration.core.services.log.audit.DatabaseServiceV2.AuditTableApi
import com.dataintegration.core.services.util.ServiceManager
import zio.{IsNotIntersection, Tag, Task, ZIO, ZLayer}

class StorageManager[T: Tag : IsNotIntersection] extends ServiceManager[FileStoreConfig] {

  val live: ZLayer[IntegrationConf with ServiceContract[FileStoreConfig, T] with DatabaseService.AuditTableApi with ServiceLayerGenericImpl[FileStoreConfig, T] with T, Nothing, StorageLive] = {
    for {
      client <- ZIO.service[T]
      service <- ZIO.service[ServiceLayerGenericImpl[FileStoreConfig, T]]
      logService <- ZIO.service[DatabaseService.AuditTableApi]
      contract <- ZIO.service[ServiceContract[FileStoreConfig, T]]
      conf <- ZIO.service[IntegrationConf]
    } yield StorageLive(client, service, contract, conf.getFileStore, logService, conf.getProperties)
  }.toLayer

  val liveManaged: ZLayer[IntegrationConf with ServiceContract[FileStoreConfig, T] with DatabaseService.AuditTableApi with ServiceLayerGenericImpl[FileStoreConfig, T] with T, Throwable, List[FileStoreConfig]] =
    live >>> Apis.startService.toManagedWith(Apis.stopService).toLayer

  case class StorageLive(
                          client: T,
                          service: ServiceLayerGenericImpl[FileStoreConfig, T],
                          contract: ServiceContract[FileStoreConfig, T],
                          fileStoreList: List[FileStoreConfig],
                          auditApi: AuditTableApi,
                          properties: Properties) extends ServiceBackend {

    override def startService: ZIO[Any, Throwable, List[FileStoreConfig]] = serviceBuilder(
      task = service.onCreate(client, contract.createService, auditApi, properties),
      listOfResources = fileStoreList,
      failureType = FailFast,
      parallelism = properties.maxParallelism)

    override def stopService(fileStoreList: List[FileStoreConfig]): ZIO[Any, Nothing, List[FileStoreConfig]] = serviceBuilder(
      task = service.onDestroy(client, contract.destroyService, auditApi, properties),
      listOfResources = fileStoreList,
      failureType = FailFast,
      parallelism = properties.maxParallelism).orDie

    override def getServiceStatus: ZIO[Any, Throwable, List[FileStoreConfig]] = Task(fileStoreList)

  }

}
