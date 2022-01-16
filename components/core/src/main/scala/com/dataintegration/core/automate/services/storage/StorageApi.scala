package com.dataintegration.core.automate.services.storage

import com.dataintegration.core.automate.services.storage.application.{CopyFiles, DeleteFiles}
import com.dataintegration.core.binders.{FileStoreConfig, Properties}
import com.dataintegration.core.services.util.ServiceLayerAuto
import zio.Task

class StorageApi[T] extends ServiceLayerAuto[FileStoreConfig, T] {

  override def onCreate(
                         client: T,
                         job: (T, FileStoreConfig) => FileStoreConfig,
                         properties: Properties)(data: FileStoreConfig): Task[FileStoreConfig] =
    CopyFiles(client, data, job, properties).execute

  override def onDestroy(
                          client: T,
                          job: (T, FileStoreConfig) => FileStoreConfig,
                          properties: Properties)(data: FileStoreConfig): Task[FileStoreConfig] =
    DeleteFiles(client, data, job, properties).execute

}
