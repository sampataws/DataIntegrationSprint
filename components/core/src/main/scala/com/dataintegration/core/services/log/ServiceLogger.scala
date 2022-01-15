package com.dataintegration.core.services.log

import com.dataintegration.core.util.ApplicationLogger
import zio.Task

/**
 * Sep class cause in future it will log to a database as well, hence the task
 */
object ServiceLogger extends ApplicationLogger {

  object Log {

    sealed trait Type

    case object LogInfo extends Type

    case object LogDebug extends Type

    case object LogError extends Type

  }

  def logAll(preText: String, text: String, logType: Log.Type = Log.LogInfo): Task[Unit] = Task {
    val format = s"[$preText] :- $text"
    logType match {
      case Log.LogInfo => logger.info(format)
      case Log.LogDebug => logger.debug(format)
      case Log.LogError => logger.error(format)
    }
  }

}
