package com.dataintegration.core.services.util

import com.dataintegration.core.binders.Properties
import com.dataintegration.core.util.ApplicationLogger
import zio.{Task, ULayer}

trait ServiceLayerV2[T, S] extends ApplicationLogger {

  val layer: ULayer[ServiceLayerV2[T, S]]

  def onCreate(client: S, properties: Properties)(data: T): Task[T]

  def onDestroy(client: S, properties: Properties)(data: T): Task[T]

  def getStatus(client: S, properties: Properties)(data: T): Task[T]

}
