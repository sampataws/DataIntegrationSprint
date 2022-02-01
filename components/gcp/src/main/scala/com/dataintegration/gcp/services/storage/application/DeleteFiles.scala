package com.dataintegration.gcp.services.storage.application

import com.dataintegration.core.binders.{FileStoreConfig, Properties}
import com.dataintegration.core.services.log.JobLogger
import com.dataintegration.core.services.util.ServiceApi
import com.dataintegration.core.util.Status
import com.dataintegration.gcp.services.GoogleUtils
import com.google.cloud.storage.Storage
import zio.Task

case class DeleteFiles(
                        client: Storage,
                        data: FileStoreConfig,
                        properties: Properties) extends ServiceApi[FileStoreConfig] {

  val className: String = getClass.getSimpleName.stripSuffix("$")

  override def preJob(): Task[Unit] =
    JobLogger.logConsole(className, s"${data.getLoggingInfo} deleting..")

  override def mainJob: Task[FileStoreConfig] = Task {
    GoogleUtils.deleteFiles(client, data)
  }

  override def postJob(serviceResult: FileStoreConfig): Task[Unit] =
    JobLogger.logConsole(className, s"${serviceResult.getLoggingInfo} deleted with status ${serviceResult.getStatus}")

  override def onSuccess: FileStoreConfig => FileStoreConfig = (data: FileStoreConfig) => data.onSuccess(Status.Success)

  override def onFailure: Throwable => FileStoreConfig = data.onFailure(Status.Failed)

  override def retries: Int = properties.maxRetries

}
