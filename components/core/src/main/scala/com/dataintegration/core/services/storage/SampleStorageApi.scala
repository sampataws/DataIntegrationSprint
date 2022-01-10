package com.dataintegration.core.services.storage

import com.dataintegration.core.binders.{FileStore, Properties}
import com.dataintegration.core.services.audit.Logging
import com.dataintegration.core.services.util.{ServiceApi, ServiceLayer}
import com.dataintegration.core.util.Status
import zio.{Task, ULayer, ZLayer}

object SampleStorageApi extends ServiceLayer[FileStore] {
  val simpleClassName: String = this.getClass.getSimpleName.replace("$","")

  override def onCreate(properties: Properties)(data: FileStore): Task[FileStore] = StorageApi(data, Status.Running, properties, simpleClassName + ".onCreate").execute
  override def onDestroy(properties: Properties)(data: FileStore): Task[FileStore] = StorageApi(data, Status.Success, properties, simpleClassName + ".onDestroy").execute
  override def getStatus(properties: Properties)(data: FileStore): Task[FileStore] = StorageApi(data, Status.Pending, properties, simpleClassName + ".getStatus").execute

  private case class StorageApi(data: FileStore, upStatus: Status.Type, properties: Properties, logText: String) extends ServiceApi[FileStore] {
    override def preJob(): Task[Unit] = Logging.atStart(data, prependString = logText)
    override def mainJob: Task[FileStore] = Task(data.copy(status = upStatus))
    override def postJob(serviceResult: FileStore): Task[Unit] = Logging.atStop(serviceResult, prependString = logText)
    override def onSuccess: () => FileStore = () => data.onSuccess(upStatus)
    override def onFailure: Throwable => FileStore = data.onFailure(Status.Failed)
    override def retries: Int = properties.maxRetries
  }

  override val layer: ULayer[ServiceLayer[FileStore]] = ZLayer.succeed(this)
}
