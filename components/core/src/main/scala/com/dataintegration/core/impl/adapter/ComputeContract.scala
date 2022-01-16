package com.dataintegration.core.impl.adapter

import com.dataintegration.core.impl.services.compute.{ComputeApi, ComputeManager}
import com.dataintegration.core.binders.{ComputeConfig, IntegrationConf}
import zio.config.ReadError
import zio.{IsNotIntersection, Tag, ZLayer}

abstract class ComputeContract[T: Tag : IsNotIntersection] extends ServiceContract[ComputeConfig, T] {

  override val api: ComputeApi[T] = new ComputeApi[T]
  override val manager: ComputeManager[T] = new ComputeManager[T]

  def deps(configLayer: ZLayer[Any, ReadError[String], IntegrationConf]): ZLayer[Any, Throwable, List[ComputeConfig]] =
    (configLayer ++ liveClient("") ++ ZLayer.succeed(api) ++ live) >>> manager.liveManaged

}

