package com.dataintegration.core.impl.adapter

import com.dataintegration.core.binders.Properties
import com.dataintegration.core.util.ApplicationLogger
import zio.Task

trait ServiceLayerGenericImpl[T, S] extends ApplicationLogger {

  def onCreate(client: S, job: (S, T) => T, properties: Properties)(data: T): Task[T]

  def onDestroy(client: S, job: (S, T) => T, properties: Properties)(data: T): Task[T]

}
