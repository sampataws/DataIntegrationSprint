package com.dataintegration.core.impl.adapter.contracts

import com.dataintegration.core.binders.{IntegrationConf, Properties}
import com.dataintegration.core.impl.adapter.ServiceLayerGenericImpl
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

  val serviceApiLive: ULayer[ServiceLayerGenericImpl[S, T]] // should be a ulayer
  val contractLive: ULayer[ServiceContract[S,T]] // should be ServiceContract[sp]

  val serviceManager: ServiceManager[S]

}
