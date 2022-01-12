package com.dataintegration.core.services.utilV2

import com.dataintegration.core.binders.Properties
import com.dataintegration.core.services.util.{ServiceConfig, ServiceResult}
import com.dataintegration.core.util.ApplicationLogger
import zio.{Task, ULayer}

trait ServiceLayer[T <: ServiceConfig, S] extends ApplicationLogger {
  val layer: ULayer[ServiceLayer[T,S]]

  def onCreate(properties: Properties)(data: T): Task[ServiceResult[T,S]]

  def onDestroy[R](properties: Properties)(data: ServiceResult[T,S]): Task[R]

  def getStatus[R](properties: Properties)(data: ServiceResult[T,S]): Task[R]

}
