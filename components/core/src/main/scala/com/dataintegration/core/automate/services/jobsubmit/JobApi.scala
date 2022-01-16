package com.dataintegration.core.automate.services.jobsubmit

import com.dataintegration.core.automate.services.jobsubmit.application.SubmitJob
import com.dataintegration.core.binders.{JobConfig, Properties}
import com.dataintegration.core.services.util.ServiceLayerAuto
import zio.Task

class JobApi[T] extends ServiceLayerAuto[JobConfig, T] {

  override def onCreate(
                         client: T,
                         job: (T, JobConfig) => JobConfig,
                         properties: Properties)(data: JobConfig): Task[JobConfig] =
    SubmitJob(client, data, job, properties).execute

  override def onDestroy(
                          client: T,
                          job: (T, JobConfig) => JobConfig,
                          properties: Properties)(data: JobConfig): Task[JobConfig] =
    Task(job(client, data))
}
