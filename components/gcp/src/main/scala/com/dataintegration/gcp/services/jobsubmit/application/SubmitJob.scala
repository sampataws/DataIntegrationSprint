package com.dataintegration.gcp.services.jobsubmit.application

import com.dataintegration.core.binders.{JobConfig, Properties}
import com.dataintegration.core.services.log.ServiceLogger
import com.dataintegration.core.services.util.ServiceApiV2
import com.dataintegration.core.util.Status
import com.dataintegration.gcp.services.GoogleUtils
import com.google.cloud.dataproc.v1.JobControllerClient
import zio.Task

case class SubmitJob(
                      client: JobControllerClient,
                      data: JobConfig,
                      properties: Properties) extends ServiceApiV2[JobConfig] {

  val className: String = getClass.getSimpleName.stripSuffix("$")

  override def preJob(): Task[Unit] =
    ServiceLogger.logConsole(className, s"${data.getLoggingInfo} job submit process started")

  override def mainJob: Task[JobConfig] = Task {
    GoogleUtils.submitSparkJob(client, data)
  }

  override def postJob(serviceResult: JobConfig): Task[Unit] =
    ServiceLogger.logConsole(className, s"${serviceResult.getLoggingInfo} job submit process completed with ${serviceResult.getStatus}")

  override def onSuccess: JobConfig => JobConfig = (data: JobConfig) => data.onSuccess(Status.Success)

  override def onFailure: Throwable => JobConfig = data.onFailure(Status.Failed)

  override def retries: Int = 0
}
