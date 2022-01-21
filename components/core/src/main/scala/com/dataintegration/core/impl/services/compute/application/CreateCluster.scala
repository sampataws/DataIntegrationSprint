package com.dataintegration.core.impl.services.compute.application

import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.services.log.JobLogger
import com.dataintegration.core.services.util.ServiceApiV2
import com.dataintegration.core.util.Status
import zio.Task

case class CreateCluster[T](
                             client: T,
                             data: ComputeConfig,
                             job: (T, ComputeConfig) => ComputeConfig,
                             properties: Properties
                           ) extends ServiceApiV2[ComputeConfig] {

  val className: String = getClass.getSimpleName.stripSuffix("$")

  override def preJob(): Task[Unit] =
    JobLogger.logConsole(className, s"${data.getLoggingInfo} creation process started")

  override def mainJob: Task[ComputeConfig] = Task {
    job(client, data)
  }

  override def postJob(serviceResult: ComputeConfig): Task[Unit] =
    JobLogger.logConsole(className, s"${serviceResult.getLoggingInfo} creation process completed with ${serviceResult.getStatus}")

  override def onSuccess: ComputeConfig => ComputeConfig =
    (data: ComputeConfig) => data.onSuccess(Status.Running)

  override def onFailure: Throwable => ComputeConfig = data.onFailure(Status.Failed)

  override def retries: Int = properties.maxRetries
}
