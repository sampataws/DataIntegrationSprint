package com.dataintegration.core.impl.services.storage.application

import com.dataintegration.core.binders.{FileStoreConfig, Properties}
import com.dataintegration.core.services.log.JobLogger
import com.dataintegration.core.services.util.ServiceApiV2
import com.dataintegration.core.util.Status
import zio.Task

case class DeleteFiles[T](
                           client: T,
                           data: FileStoreConfig,
                           job: (T, FileStoreConfig) => FileStoreConfig,
                           properties: Properties
                         ) extends ServiceApiV2[FileStoreConfig] {

  val className: String = getClass.getSimpleName.stripSuffix("$")

  override def preJob(): Task[Unit] =
    JobLogger.logConsole(className, s"${data.getLoggingInfo} deleting..")

  override def mainJob: Task[FileStoreConfig] = Task {
    job(client, data)
  }

  override def postJob(serviceResult: FileStoreConfig): Task[Unit] =
    JobLogger.logConsole(className, s"${serviceResult.getLoggingInfo} deleted with status ${serviceResult.getStatus}")

  override def onSuccess: FileStoreConfig => FileStoreConfig =
    (data: FileStoreConfig) => data.onSuccess(Status.Success)

  override def onFailure: Throwable => FileStoreConfig = data.onFailure(Status.Failed)

  override def retries: Int = properties.maxRetries

}
