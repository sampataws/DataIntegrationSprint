package com.dataintegration.core.impl.adapter

import com.dataintegration.core.binders.{ComputeConfig, IntegrationConf}
import com.dataintegration.core.impl.services.compute.{ComputeApi, ComputeManager}
import zio.config.ReadError
import zio.{IsNotIntersection, Tag, ZLayer}

abstract class ComputeContract[T: Tag : IsNotIntersection] extends ServiceContract[ComputeConfig, T] {

  override val serviceApi: ComputeApi[T] = new ComputeApi[T]
  override val serviceManager: ComputeManager[T] = new ComputeManager[T]

  def dependencies(configLayer: ZLayer[Any, ReadError[String], IntegrationConf]): ZLayer[Any, Throwable, List[ComputeConfig]] =
    (configLayer ++ (configLayer >>> clientLive) ++ ZLayer.succeed(serviceApi) ++ contractLive) >>> serviceManager.liveManaged

}

