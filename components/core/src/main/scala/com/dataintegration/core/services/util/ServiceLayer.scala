package com.dataintegration.core.services.util

import com.dataintegration.core.binders.Properties
import com.dataintegration.core.util.ApplicationLogger
import zio.Task

trait ServiceLayer[T] extends ApplicationLogger {

  def onCreate(properties: Properties)(data: T): Task[T]

  def onDestroy(properties: Properties)(data: T): Task[T]

  def getStatus(properties: Properties)(data: T): Task[T]

  def serviceBuilder(task: (T, Properties) => Task[T],
                     service: T,
                     properties: Properties): Task[T]
  = task(service, properties)

}
