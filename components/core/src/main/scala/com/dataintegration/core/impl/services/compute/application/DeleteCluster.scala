package com.dataintegration.core.impl.services.compute.application

import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.services.log.JobLogger
import com.dataintegration.core.services.log.audit.DatabaseService.AuditTableApi
import com.dataintegration.core.services.util.ServiceApi
import com.dataintegration.core.util.Status
import zio.Task

case class DeleteCluster[T](
                             client: T,
                             data: ComputeConfig,
                             job: (T, ComputeConfig) => ComputeConfig,
                             auditApi: AuditTableApi,
                             properties: Properties
                           ) extends ServiceApi[ComputeConfig] {

  val className: String = getClass.getSimpleName.stripSuffix("$")

  override def preJob(): Task[Unit] = for {
    _ <- JobLogger.logConsole(className, s"${data.getLoggingInfo} deletion process started")
    _ <- auditApi.insertInDatabase(data.getLoggingService)
  } yield ()

  override def mainJob: Task[ComputeConfig] = Task {
    job(client, data)
  }

  override def postJob(serviceResult: ComputeConfig): Task[Unit] = for {
    _ <- JobLogger.logConsole(className, s"${serviceResult.getLoggingInfo}  deletion process completed with status ${serviceResult.getStatus}")
    _ <- auditApi.updateInDatabase(serviceResult.getLoggingService)
  } yield ()

  override def onSuccess: ComputeConfig => ComputeConfig =
    (data: ComputeConfig) => data.onSuccess(Status.Success)

  override def onFailure: Throwable => ComputeConfig = data.onFailure(Status.Running)

  override def retries: Int = properties.maxRetries

}
