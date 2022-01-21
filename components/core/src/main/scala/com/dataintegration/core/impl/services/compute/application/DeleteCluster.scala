package com.dataintegration.core.impl.services.compute.application

import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.services.log.JobLogger
import com.dataintegration.core.services.util.ServiceApiV2
import com.dataintegration.core.util.Status
import zio.Task

case class DeleteCluster[T](
                             client: T,
                             data: ComputeConfig,
                             job: (T, ComputeConfig) => ComputeConfig,
                             properties: Properties
                           ) extends ServiceApiV2[ComputeConfig] {

  val className: String = getClass.getSimpleName.stripSuffix("$")

  override def preJob(): Task[Unit] =
    JobLogger.logConsole(className, s"${data.getLoggingInfo} deletion process started")

  override def mainJob: Task[ComputeConfig] = Task {
    job(client, data)
  }

  override def postJob(serviceResult: ComputeConfig): Task[Unit] =
    JobLogger.logConsole(className, s"${serviceResult.getLoggingInfo}  deletion process completed with status ${serviceResult.getStatus}")

  override def onSuccess: ComputeConfig => ComputeConfig =
    (data : ComputeConfig) => data.onSuccess(Status.Success)

  override def onFailure: Throwable => ComputeConfig = data.onFailure(Status.Running)

  override def retries: Int = properties.maxRetries

}
