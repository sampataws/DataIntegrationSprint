package com.dataintegration.core.services.util

import com.dataintegration.core.binders.Properties
import com.dataintegration.core.util.ApplicationLogger
import zio.{Task, ZIO}

trait ServiceLayer[T] extends ApplicationLogger {

  def onCreate(properties: Properties)(data: T): Task[T]

  def onDestroy(properties: Properties)(data: T): Task[T]

  def getStatus(properties: Properties)(data: T): Task[T]

  def serviceBuilder[IN, A <: ServiceConfig[IN]](task: (A, Properties) => Task[IN],
                                                 service: A,
                                                 properties: Properties): ZIO[Any, Throwable, IN] = for {
    _ <- service.logServiceStart()
    serviceResult <- task(service, properties)
      .retryN(properties.maxClusterRetries)
      .fold(service.onFailure, service.onSuccess)
    _ <- service.logServiceEnd(serviceResult)
  } yield serviceResult

}
