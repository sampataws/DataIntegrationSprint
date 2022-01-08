package com.dataintegration.core.services.audit

import com.dataintegration.core.services.util.ServiceConfig
import com.dataintegration.core.util.Status
import zio.Task

object Logging extends LoggingAndAuditApi {
  override def atStart(service: ServiceConfig): Task[Unit] = Task {
    logger.info(service.getLoggingInfo + " Started")
  }

  override def atStop(service: ServiceConfig): Task[Unit] = Task {
    val errorMessagesIfAny = if (service.getErrorMessage.isEmpty) "" else "error message :- " + service.getErrorMessage
    if (service.getStatus == Status.Failed)
      logger.error(service.getLoggingInfo + s" Ended with status ${service.getStatus} and $errorMessagesIfAny")
    else
      logger.info(service.getLoggingInfo + s" Ended with status ${service.getStatus}")
  }
}
