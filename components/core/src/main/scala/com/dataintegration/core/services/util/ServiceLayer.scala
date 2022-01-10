package com.dataintegration.core.services.util

import com.dataintegration.core.binders.Properties
import com.dataintegration.core.util.ApplicationLogger
import zio.{Task, ULayer}

trait ServiceLayer[T] extends ApplicationLogger {

  val layer: ULayer[ServiceLayer[T]]

  def onCreate(properties: Properties)(data: T): Task[T]

  def onDestroy(properties: Properties)(data: T): Task[T]

  def getStatus(properties: Properties)(data: T): Task[T]

}
