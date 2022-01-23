package com.dataintegration.core.impl.services.compute

import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.impl.adapter.ServiceLayerGenericImpl
import com.dataintegration.core.impl.services.compute.application.{CreateCluster, DeleteCluster}
import com.dataintegration.core.services.log.audit.DatabaseServiceV2.AuditTableApi
import zio.Task

class ComputeApi[T] extends ServiceLayerGenericImpl[ComputeConfig, T] {

  override def onCreate(
                         client: T,
                         job: (T, ComputeConfig) => ComputeConfig,
                         auditApi: AuditTableApi,
                         properties: Properties)(data: ComputeConfig): Task[ComputeConfig] =
    CreateCluster(client, data, job, auditApi, properties).execute

  override def onDestroy(
                          client: T,
                          job: (T, ComputeConfig) => ComputeConfig,
                          auditApi: AuditTableApi,
                          properties: Properties)(data: ComputeConfig): Task[ComputeConfig] =
    DeleteCluster(client, data, job, auditApi, properties).execute
}
