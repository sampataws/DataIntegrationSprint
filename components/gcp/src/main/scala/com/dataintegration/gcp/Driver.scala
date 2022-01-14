package com.dataintegration.gcp

import com.dataintegration.core.binders.{ComputeConfig, FileStoreConfig}
import com.dataintegration.core.services.configuration.Configuration
import com.dataintegration.core.util.ApplicationLogger
import com.dataintegration.gcp.services.compute.client.ClusterClient
import com.dataintegration.gcp.services.compute.{ComputeApi, ComputeManager}
import com.dataintegration.gcp.services.jobsubmit.client.JobClient
import com.dataintegration.gcp.services.jobsubmit.{JobApi, JobManager}
import com.dataintegration.gcp.services.storage.client.StorageClient
import com.dataintegration.gcp.services.storage.{StorageApi, StorageManager}
import zio.{ZEnv, ZIO, ZIOAppArgs, ZLayer}


object Driver extends zio.ZIOAppDefault with Configuration with ApplicationLogger {

  val endpoint = ""

  val clusterDependencies: ZLayer[Any, Throwable, List[ComputeConfig]] =
    (configLayer ++ ComputeApi.layer ++ ClusterClient.live(endpoint)) >>> ComputeManager.liveManaged

  val fileDependencies: ZLayer[Any, Throwable, List[FileStoreConfig]] =
    (configLayer ++ StorageApi.layer ++ StorageClient.live) >>> StorageManager.liveManaged

  val jobDependencies: ZLayer[Any, Throwable, JobManager.JobLive] =
    (configLayer ++ JobApi.layer ++ JobClient.live(endpoint) ++ clusterDependencies ++ fileDependencies) >>> JobManager.live

  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] =
    JobManager.Apis.startService.provideLayer(jobDependencies)
}
