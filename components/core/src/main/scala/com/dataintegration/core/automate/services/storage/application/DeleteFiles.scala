package com.dataintegration.core.automate.services.storage.application

import com.dataintegration.core.binders.{FileStoreConfig, Properties}
import com.dataintegration.core.services.log.ServiceLogger
import com.dataintegration.core.services.util.ServiceApi
import com.dataintegration.core.util.Status
import zio.Task

case class DeleteFiles[T](
                      client: T,
                      data: FileStoreConfig,
                      job: (T, FileStoreConfig) => FileStoreConfig,
                      properties: Properties
                    ) extends ServiceApi[FileStoreConfig] {

  val className: String = getClass.getSimpleName.stripSuffix("$")

  override def preJob(): Task[Unit] =
    ServiceLogger.logAll(className, s"${data.getLoggingInfo} deleting..")

  override def mainJob: Task[FileStoreConfig] = Task {
    job(client, data)
  }

  override def postJob(serviceResult: FileStoreConfig): Task[Unit] =
    ServiceLogger.logAll(className, s"${serviceResult.getLoggingInfo} deleted with status ${serviceResult.getStatus}")

  override def onSuccess: () => FileStoreConfig = () => data.onSuccess(Status.Success)

  override def onFailure: Throwable => FileStoreConfig = data.onFailure(Status.Failed)

  override def retries: Int = properties.maxRetries

}
