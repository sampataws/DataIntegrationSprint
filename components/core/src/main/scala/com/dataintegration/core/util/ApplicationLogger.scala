package com.dataintegration.core.util

import org.slf4j.{Logger, LoggerFactory}

/**
 * Trait to add logging
 */
trait ApplicationLogger {
  lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)
}
