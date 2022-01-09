package com.dataintegration.core.services.storage

import com.dataintegration.core.binders.{FileStore, Properties}
import com.dataintegration.core.services.audit.Logging
import com.dataintegration.core.services.util.{ServiceApi, ServiceLayer}
import com.dataintegration.core.util.Status
import zio.Task

object SampleStorageApi extends ServiceLayer[FileStore] {
  override def onCreate(properties: Properties)(data: FileStore): Task[FileStore] = StorageApi(data, Status.Running, properties).execute
  override def onDestroy(properties: Properties)(data: FileStore): Task[FileStore] = StorageApi(data, Status.Success, properties).execute
  override def getStatus(properties: Properties)(data: FileStore): Task[FileStore] = StorageApi(data, Status.Pending, properties).execute

  private case class StorageApi(data: FileStore, upStatus: Status.Type, properties: Properties) extends ServiceApi[FileStore] {
    override def preJob(): Task[Unit] = Logging.atStart(data)
    override def mainJob: Task[FileStore] = Task(data.copy(status = upStatus))
    override def postJob(serviceResult: FileStore): Task[Unit] = Logging.atStop(serviceResult)
    override def onSuccess: () => FileStore = () => data.onSuccess(Status.Running)
    override def onFailure: Throwable => FileStore = data.onFailure(Status.Failed)
    override def retries: Int = properties.maxRetries
  }

}
