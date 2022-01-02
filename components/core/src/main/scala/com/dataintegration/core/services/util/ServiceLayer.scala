package com.dataintegration.core.services.util

import com.dataintegration.core.binders.Properties
import com.dataintegration.core.util.ApplicationLogger
import zio.Task

trait ServiceLayer[T] extends ApplicationLogger {

  def onCreate(properties: Properties)(data: T): Task[T]

  def onDestroy(properties: Properties)(data: T): Task[T]

  def getStatus(properties: Properties)(data: T): Task[T]

  def serviceBuilder[A <: ServiceConfig](task: (A, Properties) => Task[A],
                                         service: A,
                                         properties: Properties): Task[A] = for {
    _ <- service.logServiceStart

    serviceResult <- task(service, properties)
      .retryN(properties.maxClusterRetries)
      .fold(service.onFailure, _ => service.onGenericSuccess)

    _ <- serviceResult.logServiceEnd
  } yield (service) // Todo it returns Service that's why its working should return service result

}
