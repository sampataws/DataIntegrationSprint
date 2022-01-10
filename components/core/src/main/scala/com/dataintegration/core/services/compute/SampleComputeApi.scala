package com.dataintegration.core.services.compute

import com.dataintegration.core.binders.{Cluster, Properties}
import com.dataintegration.core.services.audit.Logging
import com.dataintegration.core.services.util.{ServiceApi, ServiceLayer}
import com.dataintegration.core.util.Status
import zio.{Task, ULayer, ZLayer}

import scala.util.Random

object SampleComputeApi extends ServiceLayer[Cluster] {

  override val layer: ULayer[ServiceLayer[Cluster]] = ZLayer.succeed(this)

  override def onCreate(properties: Properties)(data: Cluster): Task[Cluster] =
    ClusterApi(data, properties).execute

  override def onDestroy(properties: Properties)(data: Cluster): Task[Cluster] =
    new ServiceApi[Cluster] {
      override def preJob(): Task[Unit] = Logging.atStart(data)

      override def mainJob: Task[Cluster] = Task(data.copy(status = Status.Running))

      override def postJob(serviceResult: Cluster): Task[Unit] = Logging.atStop(serviceResult)

      override def onSuccess: () => Cluster = () => data.onSuccess(Status.Success)

      override def onFailure: Throwable => Cluster = data.onFailure(Status.Failed)

      override def retries: Int = properties.maxRetries
    }.execute

  override def getStatus(properties: Properties)(data: Cluster): Task[Cluster] =
    new ServiceApi[Cluster] {
      override def preJob(): Task[Unit] = Logging.atStart(data)

      override def mainJob: Task[Cluster] = Task(data.copy(status = Status.Running))

      override def postJob(serviceResult: Cluster): Task[Unit] = Logging.atStop(serviceResult)

      override def onSuccess: () => Cluster = () => data.onSuccess(Status.Running)

      override def onFailure: Throwable => Cluster = data.onFailure(Status.Failed)

      override def retries: Int = properties.maxRetries
    }.execute


  private case class ClusterApi(data: Cluster, properties: Properties) extends ServiceApi[Cluster] {
    override def preJob(): Task[Unit] = Logging.atStart(data)

    def randomFailTask[T](task: T): T = if (Random.nextBoolean()) throw new Exception("Fail shame :)") else task

    override def mainJob: Task[Cluster] = Task(data.copy(status = Status.Running))

    override def postJob(serviceResult: Cluster): Task[Unit] = Logging.atStop(serviceResult)

    override def onSuccess: () => Cluster = () => data.onSuccess(Status.Running)

    override def onFailure: Throwable => Cluster = data.onFailure(Status.Failed)

    override def retries: Int = properties.maxRetries
  }

}
