package com.dataintegration.core.services.job

import com.dataintegration.core.binders.{Job, Properties}
import com.dataintegration.core.services.audit.Logging
import com.dataintegration.core.services.util.{ServiceApi, ServiceLayer}
import com.dataintegration.core.util.Status
import zio.Task

object SampleJobApi extends ServiceLayer[Job] {

  override def onCreate(properties: Properties)(data: Job): Task[Job] = JobApi(data, Status.Running, properties).execute
  override def onDestroy(properties: Properties)(data: Job): Task[Job] = JobApi(data, Status.Success, properties).execute
  override def getStatus(properties: Properties)(data: Job): Task[Job] = JobApi(data, Status.Pending, properties).execute

  private case class JobApi(data: Job, upStatus: Status.Type, properties: Properties) extends ServiceApi[Job] {
    override def preJob(): Task[Unit] = Logging.atStart(data)
    override def mainJob: Task[Job] = Task(data.copy(status = upStatus))
    override def postJob(serviceResult: Job): Task[Unit] = Logging.atStop(serviceResult)
    override def onSuccess: () => Job = () => data.onSuccess(Status.Running)
    override def onFailure: Throwable => Job = data.onFailure(Status.Failed)
    override def retries: Int = properties.maxRetries
  }

}
