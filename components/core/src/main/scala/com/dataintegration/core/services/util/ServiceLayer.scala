package com.dataintegration.core.services.util

import com.dataintegration.core.binders.Properties
import com.dataintegration.core.util.ApplicationLogger
import zio.Task

trait ServiceLayer[T] extends ApplicationLogger {

  def onCreate(properties: Properties)(data: T): Task[T]

  def onDestroy(properties: Properties)(data: T): Task[T]

  def getStatus(properties: Properties)(data: T): Task[T]

  // Good to remove :- holding for time
  //  def serviceBuilderV3(
  //                        task: => Task[T],
  //                        service: T,
  //                        onSuccess : => T,
  //                        onFailure : Throwable => T,
  //                        retries : Int): ZIO[Any, Throwable, T] =
  //    for {
  //      _ <- service.logServiceStart
  //      serviceResult <- task.retryN(retries).fold(onFailure, _ => onSuccess)
  //      _ <- serviceResult.logServiceEnd
  //    } yield serviceResult
}
