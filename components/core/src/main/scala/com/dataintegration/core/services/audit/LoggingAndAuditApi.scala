package com.dataintegration.core.services.audit

import com.dataintegration.core.services.util.ServiceConfig
import com.dataintegration.core.util.ApplicationLogger
import zio.Task

private[audit] trait LoggingAndAuditApi extends ApplicationLogger {
  def atStart(service: ServiceConfig, prependString : String): Task[Unit]

  def atStop(service: ServiceConfig, prependString : String): Task[Unit]

  def transformPrependString(prependString : String) : String =
    if(prependString.isEmpty) "" else s"[$prependString] - "
}
