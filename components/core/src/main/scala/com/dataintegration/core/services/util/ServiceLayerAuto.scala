package com.dataintegration.core.services.util

import com.dataintegration.core.binders.Properties
import com.dataintegration.core.util.ApplicationLogger
import zio.{Task, ULayer}

trait ServiceLayerAuto[T, S] extends ApplicationLogger {

  //val layer: ULayer[ServiceLayerAuto[T, S]]

  def onCreate(client: S, job: (S, T) => T, properties: Properties)(data: T): Task[T]

  def onDestroy(client: S, job: (S, T) => T, properties: Properties)(data: T): Task[T]

}
