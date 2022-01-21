package com.dataintegration.gcp.services.storage.application

import com.dataintegration.core.binders.{FileStoreConfig, Properties}
import com.dataintegration.core.services.log.ServiceLogger
import com.dataintegration.core.services.util.ServiceApiV2
import com.dataintegration.core.util.Status
import com.dataintegration.gcp.services.GoogleUtils
import com.google.cloud.storage.Storage
import zio.Task

case class CopyFiles(
                      client: Storage,
                      data: FileStoreConfig,
                      properties: Properties) extends ServiceApiV2[FileStoreConfig] {

  val className: String = getClass.getSimpleName.stripSuffix("$")

  override def preJob(): Task[Unit] =
    ServiceLogger.logConsole(className, s"${data.getLoggingInfo} copying..")

  override def mainJob: Task[FileStoreConfig] = Task {
    GoogleUtils.copyFiles(client, data)
  }

  override def postJob(serviceResult: FileStoreConfig): Task[Unit] =
    ServiceLogger.logConsole(className, s"${serviceResult.getLoggingInfo} copied with status ${serviceResult.getStatus}")

  override def onSuccess: FileStoreConfig => FileStoreConfig = (data: FileStoreConfig) => data.onSuccess(Status.Success)

  override def onFailure: Throwable => FileStoreConfig = data.onFailure(Status.Failed)

  override def retries: Int = properties.maxRetries

}
