package com.dataintegration.core.automate.adapter

import com.dataintegration.core.automate.services.jobsubmit.{JobApi, JobManager}
import com.dataintegration.core.binders.{ComputeConfig, FileStoreConfig, IntegrationConf, JobConfig}
import zio.config.ReadError
import zio.{IsNotIntersection, Tag, ZLayer}

abstract class JobSubmitContract[T: Tag : IsNotIntersection] extends ServiceContract[JobConfig, T] {

  override val api: JobApi[T] = new JobApi[T]
  override val manager: JobManager[T] = new JobManager[T]

  def deps(configLayer: ZLayer[Any, ReadError[String], IntegrationConf], clusterDep: ZLayer[Any, Throwable, List[ComputeConfig]], fileDeps: ZLayer[Any, Throwable, List[FileStoreConfig]]) =
    (configLayer ++ liveClient("") ++ ZLayer.succeed(api) ++ live ++ clusterDep ++ fileDeps) >>> manager.live

}

