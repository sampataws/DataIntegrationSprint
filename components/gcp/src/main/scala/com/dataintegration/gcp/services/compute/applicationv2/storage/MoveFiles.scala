package com.dataintegration.gcp.services.compute.applicationv2.storage

import com.dataintegration.core.binders.{FileStoreConfig, Properties}
import com.dataintegration.core.services.log.ServiceLogger
import com.dataintegration.core.services.util.ServiceApi
import com.dataintegration.core.util.Status
import com.dataintegration.gcp.services.compute.applicationv2.Utils
import com.google.cloud.storage.Storage
import zio.Task

case class MoveFiles(
                      client: Storage,
                      data: FileStoreConfig,
                      properties: Properties) extends ServiceApi[FileStoreConfig] {

  val className: String = getClass.getSimpleName.stripSuffix("$")

  override def preJob(): Task[Unit] =
    ServiceLogger.logAll(className, s"${data.getLoggingInfo} copying..")

  override def mainJob: Task[FileStoreConfig] = Task {
    Utils.copyFiles(client, data)
  }

  override def postJob(serviceResult: FileStoreConfig): Task[Unit] =
    ServiceLogger.logAll(className, s"${serviceResult.getLoggingInfo} copied with status ${serviceResult.getStatus}")

  override def onSuccess: () => FileStoreConfig = () => data.onSuccess(Status.Success)

  override def onFailure: Throwable => FileStoreConfig = data.onFailure(Status.Failed)

  override def retries: Int = properties.maxRetries

}
