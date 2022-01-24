package com.dataintegration.core.impl.adapter.contracts

import com.dataintegration.core.binders.{ComputeConfig, IntegrationConf}
import com.dataintegration.core.impl.adapter.ServiceLayerGenericImpl
import com.dataintegration.core.impl.services.Driver.ComputeContract.{contractLive, serviceManager}
import com.dataintegration.core.impl.services.compute.{ComputeApi, ComputeManager}
import com.dataintegration.core.services.log.audit.DatabaseService.AuditTableApi
import zio.config.ReadError
import zio.{IsNotIntersection, Tag, ULayer, ZLayer}

abstract class ComputeContract[T: Tag : IsNotIntersection] extends ServiceContract[ComputeConfig, T] {

  override val serviceApiLive: ULayer[ServiceLayerGenericImpl[ComputeConfig, T]] = ZLayer.succeed(new ComputeApi[T])
  override val contractLive: ULayer[ServiceContract[ComputeConfig, T]] = ZLayer.succeed(this)
  override val serviceManager: ComputeManager[T] = new ComputeManager[T]

  @deprecated
  def dependencies(configLayer: ZLayer[Any, ReadError[String], IntegrationConf], audit: ZLayer[IntegrationConf, Nothing, AuditTableApi]): ZLayer[Any, Throwable, List[ComputeConfig]] =
    (configLayer ++ (configLayer >>> audit) ++ (configLayer >>> clientLive) ++ serviceApiLive ++ contractLive) >>> serviceManager.liveManaged

  def partialDependencies: ZLayer[Any with IntegrationConf with AuditTableApi, Throwable, List[ComputeConfig]]

}

