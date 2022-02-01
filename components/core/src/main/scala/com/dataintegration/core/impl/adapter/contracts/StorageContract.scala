package com.dataintegration.core.impl.adapter.contracts

import com.dataintegration.core.binders.{FileStoreConfig, IntegrationConf}
import com.dataintegration.core.impl.adapter.ServiceLayerGenericImpl
import com.dataintegration.core.impl.services.storage.{StorageApi, StorageManager}
import com.dataintegration.core.services.log.audit.DatabaseService.AuditTableApi
import zio.config.ReadError
import zio.{IsNotIntersection, Tag, ULayer, ZLayer}

abstract class StorageContract[T: Tag : IsNotIntersection] extends ServiceContract[FileStoreConfig, T] {

  override val serviceApiLive: ULayer[ServiceLayerGenericImpl[FileStoreConfig, T]] = ZLayer.succeed(new StorageApi[T])
  override val contractLive: ULayer[ServiceContract[FileStoreConfig, T]] = ZLayer.succeed(this)
  override val serviceManager: StorageManager[T] = new StorageManager[T]

  @deprecated
  def dependencies(configLayer: ZLayer[Any, ReadError[String], IntegrationConf], audit: ZLayer[IntegrationConf, Nothing, AuditTableApi]): ZLayer[Any, Throwable, List[FileStoreConfig]] =
    (configLayer ++ (configLayer >>> audit) ++ (configLayer >>> clientLive) ++ serviceApiLive ++ contractLive) >>> serviceManager.liveManaged

  def partialDependencies: ZLayer[Any with IntegrationConf with AuditTableApi, Throwable, List[FileStoreConfig]]
}

