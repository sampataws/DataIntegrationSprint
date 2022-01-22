package com.dataintegration.core.impl.services.jobsubmit.application

import com.dataintegration.core.binders.{JobConfig, Properties}
import com.dataintegration.core.services.log.JobLogger
import com.dataintegration.core.services.log.audit.DatabaseService.AuditTableApi
import com.dataintegration.core.services.util.ServiceApi
import com.dataintegration.core.util.Status
import zio.Task

case class SubmitJob[T](
                         client: T,
                         data: JobConfig,
                         job: (T, JobConfig) => JobConfig,
                         auditApi: AuditTableApi,
                         properties: Properties
                       ) extends ServiceApi[JobConfig] {

  val className: String = getClass.getSimpleName.stripSuffix("$")

  override def preJob(): Task[Unit] = for {
    _ <- JobLogger.logConsole(className, s"${data.getLoggingInfo} job submit process started")
    _ <- auditApi.insertInDatabase(data)
  } yield ()

  override def mainJob: Task[JobConfig] = Task {
    job(client, data)
  }

  override def postJob(serviceResult: JobConfig): Task[Unit] = for {
    _ <- JobLogger.logConsole(className, s"${serviceResult.getLoggingInfo} job submit process completed with ${serviceResult.getStatus}")
    _ <- auditApi.updateInDatabase(serviceResult)
  } yield ()

  override def onSuccess: JobConfig => JobConfig =
    (data: JobConfig) => data.onSuccess(Status.Success)

  override def onFailure: Throwable => JobConfig = data.onFailure(Status.Failed)

  override def retries: Int = 0
}
