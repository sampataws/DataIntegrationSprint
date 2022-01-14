package com.dataintegration.gcp.services.jobsubmit

import com.dataintegration.core.binders.{JobConfig, Properties}
import com.dataintegration.core.services.util.ServiceLayerV2
import com.dataintegration.gcp.services.jobsubmit.application.SubmitJob
import com.google.cloud.dataproc.v1.JobControllerClient
import zio.{Task, ULayer, ZLayer}

object JobApi extends ServiceLayerV2[JobConfig, JobControllerClient] {
  override val layer: ULayer[ServiceLayerV2[JobConfig, JobControllerClient]] = ZLayer.succeed(this)

  override def onCreate(client: JobControllerClient, properties: Properties)(data: JobConfig): Task[JobConfig] =
    SubmitJob(client, data, properties).execute

  override def onDestroy(client: JobControllerClient, properties: Properties)(data: JobConfig): Task[JobConfig] =
    Task(data)

  override def getStatus(client: JobControllerClient, properties: Properties)(data: JobConfig): Task[JobConfig] =
    Task(data)
}
