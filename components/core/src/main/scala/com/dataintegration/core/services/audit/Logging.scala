package com.dataintegration.core.services.audit

import com.dataintegration.core.services.util.ServiceConfig
import com.dataintegration.core.util.Status
import zio.Task

object Logging extends LoggingAndAuditApi {
  override def atStart(service: ServiceConfig, prependString: String = ""): Task[Unit] = Task {
    logger.info(transformPrependString(prependString) + service.getLoggingInfo + " Started")
  }

  override def atStop(service: ServiceConfig, prependString: String = ""): Task[Unit] = Task {
    val errorMessagesIfAny = if (service.getErrorMessage.isEmpty) "" else "error message :- " + service.getErrorMessage
    if (service.getStatus == Status.Failed)
      logger.error(transformPrependString(prependString) + service.getLoggingInfo + s" Ended with status ${service.getStatus} and $errorMessagesIfAny")
    else
      logger.info(transformPrependString(prependString) + service.getLoggingInfo + s" Ended with status ${service.getStatus}")
  }
}
