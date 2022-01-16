package com.dataintegration.core.impl.adapter

import com.dataintegration.core.impl.services.jobsubmit.{JobApi, JobManager}
import com.dataintegration.core.binders.{ComputeConfig, FileStoreConfig, IntegrationConf, JobConfig}
import zio.config.ReadError
import zio.{IsNotIntersection, Tag, ZLayer}

abstract class JobContract[T: Tag : IsNotIntersection] extends ServiceContract[JobConfig, T] {

  override val serviceApi: JobApi[T] = new JobApi[T]
  override val serviceManager: JobManager[T] = new JobManager[T]

  def dependencies(configLayer: ZLayer[Any, ReadError[String], IntegrationConf], clusterDependencies: ZLayer[Any, Throwable, List[ComputeConfig]], fileDependencies: ZLayer[Any, Throwable, List[FileStoreConfig]]): ZLayer[Any, Throwable, serviceManager.JobLive] =
    (configLayer ++ (configLayer >>> clientLive) ++ ZLayer.succeed(serviceApi) ++ contractLive ++ clusterDependencies ++ fileDependencies) >>> serviceManager.live

}

