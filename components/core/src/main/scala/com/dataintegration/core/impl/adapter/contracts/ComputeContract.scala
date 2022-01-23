package com.dataintegration.core.impl.adapter.contracts

import com.dataintegration.core.binders.{ComputeConfig, IntegrationConf}
import com.dataintegration.core.impl.services.compute.{ComputeApi, ComputeManager}
import com.dataintegration.core.services.log.audit.DatabaseServiceV2.AuditTableApi
import zio.config.ReadError
import zio.{IsNotIntersection, Tag, ZLayer}

abstract class ComputeContract[T: Tag : IsNotIntersection] extends ServiceContract[ComputeConfig, T] {

  override val serviceApi: ComputeApi[T] = new ComputeApi[T]
  override val serviceManager: ComputeManager[T] = new ComputeManager[T]

  def dependencies(configLayer: ZLayer[Any, ReadError[String], IntegrationConf], audit: ZLayer[IntegrationConf, Nothing, AuditTableApi]): ZLayer[Any, Throwable, List[ComputeConfig]] =
    (configLayer ++ (configLayer >>> audit) ++ (configLayer >>> clientLive) ++ ZLayer.succeed(serviceApi) ++ contractLive) >>> serviceManager.liveManaged

}

