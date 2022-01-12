package com.dataintegration.core.services.job

import com.dataintegration.core.binders.{Cluster, FileStore}
import com.dataintegration.core.configuration.Configuration
import com.dataintegration.core.services.compute.{ComputeManager, SampleComputeApi}
import com.dataintegration.core.services.storage.{SampleStorageApi, StorageManager}
import com.dataintegration.core.util.ApplicationLogger
import zio.{ZEnv, ZIO, ZIOAppArgs, ZLayer}

object Test extends zio.ZIOAppDefault with Configuration with ApplicationLogger {

  val clusterDependencies: ZLayer[Any, Throwable, List[Cluster]] =
    (configLayer ++ SampleComputeApi.layer) >>> ComputeManager.liveManaged

  val storageDependencies: ZLayer[Any, Throwable, List[FileStore]] =
    configLayer ++ SampleStorageApi.layer >>> StorageManager.liveManaged

  val jobDependencies: ZLayer[Any, Throwable, JobManager.JobManagerLive] =
    (configLayer ++ SampleJobApi.layer ++ clusterDependencies ++ storageDependencies) >>> JobManager.live

  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] =
    JobManager.Apis.startService.provideLayer(jobDependencies)
}
