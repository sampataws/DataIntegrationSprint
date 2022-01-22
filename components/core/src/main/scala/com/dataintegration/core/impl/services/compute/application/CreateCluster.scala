package com.dataintegration.core.impl.services.compute.application

import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.services.log.JobLogger
import com.dataintegration.core.services.log.audit.DatabaseService.AuditTableApi
import com.dataintegration.core.services.util.ServiceApi
import com.dataintegration.core.util.Status
import zio.Task

case class CreateCluster[T](
                             client: T,
                             data: ComputeConfig,
                             job: (T, ComputeConfig) => ComputeConfig,
                             auditApi: AuditTableApi,
                             properties: Properties
                           ) extends ServiceApi[ComputeConfig] {

  val className: String = getClass.getSimpleName.stripSuffix("$")

  override def preJob(): Task[Unit] = for {
    _ <- JobLogger.logConsole(className, s"${data.getLoggingInfo} creation process started")
    _ <- auditApi.insertInDatabase(data.getLoggingService)
  } yield()

  override def mainJob: Task[ComputeConfig] = Task {
    job(client, data)
  }

  override def postJob(serviceResult: ComputeConfig): Task[Unit] = for {
    _ <- JobLogger.logConsole(className, s"${serviceResult.getLoggingInfo} creation process completed with ${serviceResult.getStatus}")
    _ <- auditApi.updateInDatabase(serviceResult.getLoggingService)
  } yield ()

  override def onSuccess: ComputeConfig => ComputeConfig =
    (data: ComputeConfig) => data.onSuccess(Status.Running)

  override def onFailure: Throwable => ComputeConfig = data.onFailure(Status.Failed)

  override def retries: Int = properties.maxRetries
}
