package com.dataintegration.core.impl.adapter

import com.dataintegration.core.impl.services.storage.{StorageApi, StorageManager}
import com.dataintegration.core.binders.{FileStoreConfig, IntegrationConf}
import zio.config.ReadError
import zio.{IsNotIntersection, Tag, ZLayer}

abstract class StorageContract[T: Tag : IsNotIntersection] extends ServiceContract[FileStoreConfig, T] {

  override val serviceApi: StorageApi[T] = new StorageApi[T]
  override val serviceManager: StorageManager[T] = new StorageManager[T]

  def dependencies(configLayer: ZLayer[Any, ReadError[String], IntegrationConf]): ZLayer[Any, Throwable, List[FileStoreConfig]] =
    (configLayer ++ (configLayer >>> clientLive) ++ ZLayer.succeed(serviceApi) ++ contractLive) >>> serviceManager.liveManaged

}

