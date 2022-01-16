package com.dataintegration.core.services.util

import zio.{Task, URIO, ZLayer}

trait ServiceContract[S <: ServiceConfig, T] {

  def createClient(endpoint: String): Task[T]
  def destroyClient(client: T): URIO[Any, Unit]

  def createService(client: T, data: S): S
  def destroyService(client: T, data: S): S

  def liveClient(endpoint: String) : ZLayer[Any, Throwable, T]

  //  def live(endpoint: String): ZLayer[Any, Throwable, T] =
  //    ZManaged.acquireReleaseWith(acquire = createClient(endpoint))(release = client => destroyClient(client)).toLayer
}
