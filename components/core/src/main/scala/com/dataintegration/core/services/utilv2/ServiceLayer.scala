package com.dataintegration.core.services.utilv2

import com.dataintegration.core.binders.Properties
import com.dataintegration.core.services.util.{ServiceConfig, ServiceResult}
import com.dataintegration.core.util.ApplicationLogger
import zio.{Task, ULayer}

trait ServiceLayer[CONFIG <: ServiceConfig, RESULT, CLIENT] extends ApplicationLogger {
  val layer: ULayer[ServiceLayer[CONFIG, RESULT, CLIENT]]

  def onCreate(properties: Properties, client: CLIENT)(data: CONFIG): Task[ServiceResult[CONFIG, RESULT]]

  def onDestroy(properties: Properties, client: CLIENT)(data: ServiceResult[CONFIG, RESULT]): Task[CONFIG]

  def getStatus(properties: Properties, client: CLIENT)(data: ServiceResult[CONFIG, RESULT]): Task[RESULT]

}
