package com.dataintegration.gcp.services.compute.application

import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.services.utilv2.{ServiceApi, ServiceResult}
import com.dataintegration.core.util.Status
import com.dataintegration.gcp.services.compute.applicationv2.Utils
import com.google.cloud.dataproc.v1.{Cluster, ClusterControllerClient}
import zio.{Task, ZIO}

case class CreateCluster(
                          data: ComputeConfig,
                          properties: Properties,
                          client: ClusterControllerClient) extends ServiceApi[ComputeConfig, Cluster] {

  override def preJob(): Task[Unit] = ZIO.unit

  override def mainJob: Task[Cluster] = Task(Utils.createDataprocCluster(data, client))

  override def postJob(serviceResult: ServiceResult[ComputeConfig, Cluster]): Task[Unit] = ZIO.unit

  override def onSuccess: Cluster => ServiceResult[ComputeConfig, Cluster] =
    (cls: Cluster) => ServiceResult(data.copy(status = Status.Running), Some(cls))

  override def onFailure: Throwable => ServiceResult[ComputeConfig, Cluster] =
    (failure: Throwable) => ServiceResult(data.copy(status = Status.Failed), None)

  override def retries: Int = properties.maxRetries
}
