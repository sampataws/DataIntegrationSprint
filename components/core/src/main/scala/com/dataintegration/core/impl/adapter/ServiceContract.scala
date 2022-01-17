package com.dataintegration.core.impl.adapter

import com.dataintegration.core.binders.{IntegrationConf, Properties}
import com.dataintegration.core.services.util.{ServiceConfig, ServiceManager}
import com.dataintegration.core.util.ApplicationLogger
import zio.{IsNotIntersection, Tag, UIO, ULayer, ZIO, ZLayer}

abstract class ServiceContract[S <: ServiceConfig, T: Tag : IsNotIntersection] extends ApplicationLogger {

  def createClient(properties: Properties): T
  def destroyClient(client: T): Unit

  def createService(client: T, data: S): S
  def destroyService(client: T, data: S): S

  def clientLive: ZLayer[IntegrationConf, Nothing, T] =
    ZIO.service[IntegrationConf]
      .map(prop => createClient(prop.getProperties))
      .toManagedWith(client => UIO(destroyClient(client))).toLayer

  val serviceApi: ServiceLayerGenericImpl[S, T]
  val serviceManager: ServiceManager[S]
  val contractLive: ULayer[this.type]

}
