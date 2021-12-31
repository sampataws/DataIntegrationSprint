package com.dataintegration.core.util

import org.slf4j.{Logger, LoggerFactory}

/**
 * Trait to add logging
 */
trait ApplicationLogger {
  protected lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)
}
