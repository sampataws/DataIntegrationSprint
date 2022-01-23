package com.dataintegration.core.impl.adapter

import com.dataintegration.core.binders.Properties
import com.dataintegration.core.services.log.audit.DatabaseServiceV2.AuditTableApi
import com.dataintegration.core.util.ApplicationLogger
import zio.Task

trait ServiceLayerGenericImpl[T, S] extends ApplicationLogger {

  def onCreate(client: S, job: (S, T) => T, auditApi: AuditTableApi, properties: Properties)(data: T): Task[T]

  def onDestroy(client: S, job: (S, T) => T, auditApi: AuditTableApi, properties: Properties)(data: T): Task[T]

}
