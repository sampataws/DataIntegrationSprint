package com.dataintegration.core.impl.adapter

import com.dataintegration.core.binders.{IntegrationConf, Properties}
import com.dataintegration.core.services.util.{ServiceConfig, ServiceManager}
import zio.{IsNotIntersection, Tag, ULayer, URIO, ZIO, ZLayer}

abstract class ServiceContract[S <: ServiceConfig, T: Tag : IsNotIntersection] {

  def createClient(properties: Properties): T
  def destroyClient(client: T): URIO[Any, Unit]

  def createService(client: T, data: S): S
  def destroyService(client: T, data: S): S

  def clientLive: ZLayer[IntegrationConf, Nothing, T] =
    ZIO.service[IntegrationConf].map(prop => createClient(prop.getProperties)).toManagedWith(destroyClient).toLayer

  val serviceApi: ServiceLayerGenericImpl[S, T]
  val serviceManager: ServiceManager[S]
  val contractLive: ULayer[this.type]

}
