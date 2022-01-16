package com.dataintegration.core.automate.adapter

import com.dataintegration.core.automate.services.storage.{StorageApi, StorageManager}
import com.dataintegration.core.binders.{FileStoreConfig, IntegrationConf}
import zio.config.ReadError
import zio.{IsNotIntersection, Tag, ZLayer}

abstract class StorageContract[T: Tag : IsNotIntersection] extends ServiceContract[FileStoreConfig, T] {

  override val api: StorageApi[T] = new StorageApi[T]
  override val manager: StorageManager[T] = new StorageManager[T]

  def deps(configLayer: ZLayer[Any, ReadError[String], IntegrationConf]): ZLayer[Any, Throwable, List[FileStoreConfig]] =
    (configLayer ++ liveClient("") ++ ZLayer.succeed(api) ++ live) >>> manager.liveManaged

}

