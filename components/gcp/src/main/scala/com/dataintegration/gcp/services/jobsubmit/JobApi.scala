package com.dataintegration.gcp.services.jobsubmit

import com.dataintegration.core.binders.{JobConfig, Properties}
import com.dataintegration.core.services.util.ServiceLayer
import com.dataintegration.gcp.services.jobsubmit.application.SubmitJob
import com.google.cloud.dataproc.v1.JobControllerClient
import zio.{Task, ULayer, ZLayer}

object JobApi extends ServiceLayer[JobConfig, JobControllerClient] {
  override val layer: ULayer[ServiceLayer[JobConfig, JobControllerClient]] = ZLayer.succeed(this)

  override def onCreate(client: JobControllerClient, properties: Properties)(data: JobConfig): Task[JobConfig] =
    SubmitJob(client, data, properties).execute

  override def onDestroy(client: JobControllerClient, properties: Properties)(data: JobConfig): Task[JobConfig] =
    Task(data)

  override def getStatus(client: JobControllerClient, properties: Properties)(data: JobConfig): Task[JobConfig] =
    Task(data)
}
