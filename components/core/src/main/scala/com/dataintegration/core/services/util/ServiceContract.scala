package com.dataintegration.core.services.util

import com.dataintegration.core.automate.services.compute.{ComputeApi, ComputeManager}
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

  val newApi = new ComputeApi[T]
  val newManager = new ComputeManager[T]



  //def partialDep2: ZLayer[Any, Throwable, T with ServiceLayerAuto[S, T] with ServiceContract.this.type] = null
}
