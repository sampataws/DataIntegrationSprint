package com.dataintegration.gcp.services.compute.applicationv2

import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.services.log.ServiceLogger
import com.dataintegration.core.services.util.ServiceApi
import com.dataintegration.core.util.Status
import com.dataintegration.gcp.services.Utils
import com.google.cloud.dataproc.v1.ClusterControllerClient
import zio.Task

case class CreateCluster(
                          client: ClusterControllerClient,
                          data: ComputeConfig,
                          properties: Properties) extends ServiceApi[ComputeConfig] {

  val className: String = getClass.getSimpleName.stripSuffix("$")

  override def preJob(): Task[Unit] =
    ServiceLogger.logAll(className, s"${data.getLoggingInfo} creation process started")

  override def mainJob: Task[ComputeConfig] = Task {
    Utils.createDataprocCluster(data, client)
    data.copy(status = Status.Running)
  }

  override def postJob(serviceResult: ComputeConfig): Task[Unit] =
    ServiceLogger.logAll(className,s"${serviceResult.getLoggingInfo} creation process completed with ${serviceResult.getStatus}")

  override def onSuccess: () => ComputeConfig = () => data.onSuccess(Status.Running)

  override def onFailure: Throwable => ComputeConfig = data.onFailure(Status.Failed)

  override def retries: Int = properties.maxRetries
}
