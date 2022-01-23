package com.dataintegration.core.impl.services.jobsubmit

import com.dataintegration.core.binders.{JobConfig, Properties}
import com.dataintegration.core.impl.adapter.ServiceLayerGenericImpl
import com.dataintegration.core.impl.services.jobsubmit.application.SubmitJob
import com.dataintegration.core.services.log.audit.DatabaseService.AuditTableApi
import zio.Task

class JobApi[T] extends ServiceLayerGenericImpl[JobConfig, T] {

  override def onCreate(
                         client: T,
                         job: (T, JobConfig) => JobConfig,
                         auditApi: AuditTableApi,
                         properties: Properties)(data: JobConfig): Task[JobConfig] =
    SubmitJob(client, data, job, auditApi, properties).execute

  override def onDestroy(
                          client: T,
                          job: (T, JobConfig) => JobConfig,
                          auditApi: AuditTableApi,
                          properties: Properties)(data: JobConfig): Task[JobConfig] =
    Task(job(client, data))
}
