package com.dataintegration.gcp.services.storage

import com.dataintegration.core.binders.{FileStoreConfig, Properties}
import com.dataintegration.core.services.util.ServiceLayerV2
import com.dataintegration.gcp.services.storage.application.{DeleteFiles, CopyFiles}
import com.google.cloud.storage.Storage
import zio.{Task, ULayer, ZLayer}

object StorageApi extends ServiceLayerV2[FileStoreConfig, Storage]{

  override val layer: ULayer[ServiceLayerV2[FileStoreConfig, Storage]] = ZLayer.succeed(this)

  override def onCreate(client: Storage, properties: Properties)(data: FileStoreConfig): Task[FileStoreConfig] =
    CopyFiles(client, data, properties).execute

  override def onDestroy(client: Storage, properties: Properties)(data: FileStoreConfig): Task[FileStoreConfig] =
    DeleteFiles(client, data, properties).execute

  override def getStatus(client: Storage, properties: Properties)(data: FileStoreConfig): Task[FileStoreConfig] =
    Task(data)
}
