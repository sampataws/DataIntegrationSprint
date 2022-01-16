package com.dataintegration.core.automate.adapter

import com.dataintegration.core.services.util.{ServiceConfig, ServiceLayerAuto, ServiceManager}
import zio.{IsNotIntersection, Tag, Task, ULayer, URIO, ZLayer}

abstract class ServiceContract[S <: ServiceConfig, T: Tag : IsNotIntersection] {

  def createClient(endpoint: String): Task[T]
  def destroyClient(client: T): URIO[Any, Unit]

  def createService(client: T, data: S): S
  def destroyService(client: T, data: S): S

  def liveClient(endpoint: String): ZLayer[Any, Throwable, T]

  val api: ServiceLayerAuto[S, T]
  val manager: ServiceManager[S]
  val live: ULayer[this.type]

}
