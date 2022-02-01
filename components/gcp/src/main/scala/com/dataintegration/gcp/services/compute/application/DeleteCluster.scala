package com.dataintegration.gcp.services.compute.application

import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.services.log.JobLogger
import com.dataintegration.core.services.util.ServiceApi
import com.dataintegration.core.util.Status
import com.dataintegration.gcp.services.GoogleUtils
import com.google.cloud.dataproc.v1.ClusterControllerClient
import zio.Task

case class DeleteCluster(
                          client: ClusterControllerClient,
                          data: ComputeConfig,
                          properties: Properties) extends ServiceApi[ComputeConfig] {

  val className: String = getClass.getSimpleName.stripSuffix("$")

  override def preJob(): Task[Unit] =
    JobLogger.logConsole(className, s"${data.getLoggingInfo} deletion process started")

  override def mainJob: Task[ComputeConfig] = Task {
    GoogleUtils.deleteCluster(data, client)
  }

  override def postJob(serviceResult: ComputeConfig): Task[Unit] =
    JobLogger.logConsole(className, s"${serviceResult.getLoggingInfo}  deletion process completed with status ${serviceResult.getStatus}")

  override def onSuccess: ComputeConfig => ComputeConfig = (data: ComputeConfig) => data.onSuccess(Status.Success)

  override def onFailure: Throwable => ComputeConfig = data.onFailure(Status.Running)

  override def retries: Int = properties.maxRetries
}
