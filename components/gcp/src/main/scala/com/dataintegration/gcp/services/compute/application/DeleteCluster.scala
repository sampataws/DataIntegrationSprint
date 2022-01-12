package com.dataintegration.gcp.services.compute.application

import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.services.utilv2.{ServiceApi, ServiceResult}
import com.dataintegration.core.util.Status
import com.google.cloud.dataproc.v1.{Cluster, ClusterControllerClient}
import zio.{Task, ZIO}

case class DeleteCluster(
                          data: ServiceResult[ComputeConfig, Cluster],
                          properties: Properties,
                          client: ClusterControllerClient) extends ServiceApi[ComputeConfig, Unit] {

  override def preJob(): Task[Unit] = ZIO.unit

  override def mainJob: Task[Unit] = Task(Utils.deleteCluster(data.config, data.result.get, client)).unit

  override def postJob(serviceResult: ServiceResult[ComputeConfig, Unit]): Task[Unit] = ZIO.unit

  override def onSuccess: Unit => ServiceResult[ComputeConfig, Unit] = _ => ServiceResult(data.config.copy(status = Status.Success), None)

  override def onFailure: Throwable => ServiceResult[ComputeConfig, Unit] =
    (failure: Throwable) => ServiceResult(data.config.copy(status = Status.Failed), None)

  override def retries: Int = properties.maxRetries
}
