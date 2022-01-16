package com.dataintegration.core.automate.services

import com.dataintegration.core.automate.services.compute.{ComputeApi, ComputeManager}
import com.dataintegration.core.automate.services.jobsubmit.{JobApi, JobManager}
import com.dataintegration.core.automate.services.storage.{StorageApi, StorageManager}
import com.dataintegration.core.binders.{ComputeConfig, FileStoreConfig, JobConfig}
import com.dataintegration.core.services.configuration.Configuration
import com.dataintegration.core.services.util.ServiceContract
import com.dataintegration.core.util.{ApplicationLogger, Status}
import zio.{Task, URIO, ZEnv, ZIO, ZIOAppArgs, ZLayer, ZManaged}

object Driver extends zio.ZIOAppDefault with Configuration with ApplicationLogger {

  def debugStr = s"[${Thread.currentThread().getName}] :- "

  object ComputeContract extends ServiceContract[ComputeConfig, String] {
    override def createClient(endpoint: String): Task[String] = Task("ComputeContract Started").debug(debugStr)
    override def destroyClient(client: String): URIO[Any, Unit] = Task("ComputeContract Ended").debug(debugStr).unit.orDie
    override def createService(client: String, data: ComputeConfig): ComputeConfig = {
      logger.info(s"Cluster create started $client")
      data.copy(status = Status.Running)
    }

    override def destroyService(client: String, data: ComputeConfig): ComputeConfig = {
      logger.info(s"Cluster create done $client")
      data.copy(status = Status.Success)
    }
    override def liveClient(endpoint: String): ZLayer[Any, Throwable, String] =
      ZManaged.acquireReleaseWith(acquire = createClient("Some"))(release = client => destroyClient(client)).toLayer

    val live = ZLayer.succeed(this)
  }

  object StorageContract extends ServiceContract[FileStoreConfig, String] {
    override def createClient(endpoint: String): Task[String] = Task("StorageContract Started").debug(debugStr)
    override def destroyClient(client: String): URIO[Any, Unit] = Task("StorageContract Ended").debug(debugStr).unit.orDie
    override def createService(client: String, data: FileStoreConfig): FileStoreConfig = {
      logger.info(s"Storage create started $client")
      data.copy(status = Status.Running)
    }
    override def destroyService(client: String, data: FileStoreConfig): FileStoreConfig = {
      logger.info(s"Storage create done $client")
      data.copy(status = Status.Success)
    }
    override def liveClient(endpoint: String): ZLayer[Any, Throwable, String] =
      ZManaged.acquireReleaseWith(acquire = createClient("Some"))(release = client => destroyClient(client)).toLayer

    val live = ZLayer.succeed(this)
  }

  object JobContract extends ServiceContract[JobConfig, String] {
    override def createClient(endpoint: String): Task[String] = Task("JobContract Started").debug(debugStr)
    override def destroyClient(client: String): URIO[Any, Unit] = Task("JobContract Ended").debug(debugStr).unit.orDie
    override def createService(client: String, data: JobConfig): JobConfig = {
      logger.info(s"Job create started $client")
      data.copy(status = Status.Running)
    }
    override def destroyService(client: String, data: JobConfig): JobConfig = {
      logger.info(s"Job create done $client")
      data.copy(status = Status.Success)
    }
    override def liveClient(endpoint: String): ZLayer[Any, Throwable, String] =
      ZManaged.acquireReleaseWith(acquire = createClient("Some"))(release = client => destroyClient(client)).toLayer

    val live = ZLayer.succeed(this)
  }


  val (computeManager, computeApi) = (new ComputeManager[String], new ComputeApi[String])
  val (storageManager, storageApi) = (new StorageManager[String], new StorageApi[String])
  val (jobManager, jobApi) = (new JobManager[String], new JobApi[String])

  val endpoint = "us"

  val clusterDependencies =
    (configLayer ++ ComputeContract.liveClient(endpoint) ++ ZLayer.succeed(computeApi) ++ ComputeContract.live) >>> computeManager.liveManaged

  val fileDependencies =
    (configLayer ++ StorageContract.liveClient(endpoint) ++ ZLayer.succeed(storageApi) ++ StorageContract.live) >>> storageManager.liveManaged

  val jobDependencies =
    (configLayer ++ JobContract.liveClient(endpoint) ++ ZLayer.succeed(jobApi) ++ JobContract.live ++ clusterDependencies ++ fileDependencies) >>> jobManager.live

  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] =
    jobManager.Apis.startService.provideLayer(jobDependencies)
}
