package com.dataintegration.gcp.services.compute.application

import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.services.log.ServiceLogger
import com.dataintegration.core.services.util.ServiceApiV2
import com.dataintegration.core.util.Status
import com.dataintegration.gcp.services.GoogleUtils
import com.google.cloud.dataproc.v1.ClusterControllerClient
import zio.Task

case class CreateCluster(
                          client: ClusterControllerClient,
                          data: ComputeConfig,
                          properties: Properties) extends ServiceApiV2[ComputeConfig] {

  val className: String = getClass.getSimpleName.stripSuffix("$")

  override def preJob(): Task[Unit] =
    ServiceLogger.logConsole(className, s"${data.getLoggingInfo} creation process started")

  override def mainJob: Task[ComputeConfig] = Task {
    GoogleUtils.createDataprocCluster(data, client)
  }

  override def postJob(serviceResult: ComputeConfig): Task[Unit] =
    ServiceLogger.logConsole(className, s"${serviceResult.getLoggingInfo} creation process completed with ${serviceResult.getStatus}")

  override def onSuccess: ComputeConfig => ComputeConfig = (data: ComputeConfig) => data.onSuccess(Status.Running)

  override def onFailure: Throwable => ComputeConfig = data.onFailure(Status.Failed)

  override def retries: Int = properties.maxRetries
}
