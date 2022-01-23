package com.dataintegration.core.impl.services.storage

import com.dataintegration.core.binders.{FileStoreConfig, Properties}
import com.dataintegration.core.impl.adapter.ServiceLayerGenericImpl
import com.dataintegration.core.impl.services.storage.application.{CopyFiles, DeleteFiles}
import com.dataintegration.core.services.log.audit.DatabaseService.AuditTableApi
import zio.Task

class StorageApi[T] extends ServiceLayerGenericImpl[FileStoreConfig, T] {

  override def onCreate(
                         client: T,
                         job: (T, FileStoreConfig) => FileStoreConfig,
                         auditApi: AuditTableApi,
                         properties: Properties)(data: FileStoreConfig): Task[FileStoreConfig] =
    CopyFiles(client, data, job, auditApi, properties).execute

  override def onDestroy(
                          client: T,
                          job: (T, FileStoreConfig) => FileStoreConfig,
                          auditApi: AuditTableApi,
                          properties: Properties)(data: FileStoreConfig): Task[FileStoreConfig] =
    DeleteFiles(client, data, job, auditApi, properties).execute

}
