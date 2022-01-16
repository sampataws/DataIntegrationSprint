package com.dataintegration.core.automate.services.compute.application

import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.services.log.ServiceLogger
import com.dataintegration.core.services.util.ServiceApi
import com.dataintegration.core.util.Status
import zio.Task

case class DeleteCluster[T](
                          client: T,
                          data: ComputeConfig,
                          job: (T, ComputeConfig) => ComputeConfig,
                          properties: Properties
                        ) extends ServiceApi[ComputeConfig] {

  val className: String = getClass.getSimpleName.stripSuffix("$")

  override def preJob(): Task[Unit] =
    ServiceLogger.logAll(className, s"${data.getLoggingInfo} deletion process started")

  override def mainJob: Task[ComputeConfig] = Task {
    job(client, data)
  }

  override def postJob(serviceResult: ComputeConfig): Task[Unit] =
    ServiceLogger.logAll(className, s"${serviceResult.getLoggingInfo}  deletion process completed with status ${serviceResult.getStatus}")

  override def onSuccess: () => ComputeConfig = () => data.onSuccess(Status.Success)

  override def onFailure: Throwable => ComputeConfig = data.onFailure(Status.Running)

  override def retries: Int = properties.maxRetries

}
