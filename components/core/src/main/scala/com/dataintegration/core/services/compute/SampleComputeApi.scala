package com.dataintegration.core.services.compute

import com.dataintegration.core.binders.{Cluster, Properties}
import com.dataintegration.core.services.util.{ServiceApi, ServiceLayer}
import com.dataintegration.core.util.Status
import zio.Task

object SampleComputeApi extends ServiceLayer[Cluster] {

  override def onCreate(properties: Properties)(data: Cluster): Task[Cluster] =
    ClusterApi(data, properties).execute

  override def onDestroy(properties: Properties)(data: Cluster): Task[Cluster] =
    ClusterApi(data, properties).execute

  override def getStatus(properties: Properties)(data: Cluster): Task[Cluster] =
    new ServiceApi[Cluster] {
      override def preJob(): Task[String] = data.logServiceStart

      override def mainJob: Task[Cluster] = Task(data.copy(status = Status.Running))

      override def postJob(serviceResult: Cluster): Task[String] = serviceResult.logServiceEnd

      override def onSuccess: () => Cluster = () => data.onSuccess(Status.Running)

      override def onFailure: Throwable => Cluster = data.onFailure(Status.Failed)

      override def retries: Int = properties.maxClusterRetries
    }.execute


  private case class ClusterApi(data: Cluster, properties: Properties) extends ServiceApi[Cluster] {
    override def preJob(): Task[String] = data.logServiceStart

    override def mainJob: Task[Cluster] = Task(data.copy(status = Status.Running))

    override def postJob(serviceResult: Cluster): Task[String] = serviceResult.logServiceEnd

    override def onSuccess: () => Cluster = () => data.onSuccess(Status.Running)

    override def onFailure: Throwable => Cluster = data.onFailure(Status.Failed)

    override def retries: Int = properties.maxClusterRetries
  }

}
