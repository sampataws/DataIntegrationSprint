package com.dataintegration.core.impl.adapter.contracts

import com.dataintegration.core.binders.{ComputeConfig, FileStoreConfig, IntegrationConf, JobConfig}
import com.dataintegration.core.impl.adapter.ServiceLayerGenericImpl
import com.dataintegration.core.impl.services.jobsubmit.{JobApi, JobManager}
import com.dataintegration.core.services.log.audit.DatabaseService.AuditTableApi
import zio.config.ReadError
import zio.{IsNotIntersection, Tag, ULayer, ZLayer}

abstract class JobContract[T: Tag : IsNotIntersection] extends ServiceContract[JobConfig, T] {

  override val serviceApiLive: ULayer[ServiceLayerGenericImpl[JobConfig, T]] = ZLayer.succeed(new JobApi[T])
  override val contractLive: ULayer[ServiceContract[JobConfig, T]] = ZLayer.succeed(this)
  override val serviceManager: JobManager[T] = new JobManager[T]

  @deprecated
  def dependencies(configLayer: ZLayer[Any, ReadError[String], IntegrationConf], audit: ZLayer[IntegrationConf, Nothing, AuditTableApi], clusterDependencies: ZLayer[Any, Throwable, List[ComputeConfig]], fileDependencies: ZLayer[Any, Throwable, List[FileStoreConfig]]): ZLayer[Any, Throwable, serviceManager.JobLive] =
    (configLayer ++ (configLayer >>> audit) ++ (configLayer >>> clientLive) ++ serviceApiLive ++ contractLive ++ clusterDependencies ++ fileDependencies) >>> serviceManager.live

  def partialDependencies: ZLayer[Any with IntegrationConf with AuditTableApi with List[ComputeConfig] with List[FileStoreConfig], Nothing, serviceManager.JobLive]
}

