package com.dataintegration.core.impl.services

/*

package com.dataintegration.core.services.util

import com.dataintegration.core.automate.services.compute.{ComputeApi, ComputeManager}
import zio.{IsNotIntersection, Tag, Task, ULayer, URIO, ZLayer}

abstract class ServiceContract[S <: ServiceConfig, T: Tag : IsNotIntersection] {

  def createClient(endpoint: String): Task[T]
  def destroyClient(client: T): URIO[Any, Unit]

  def createService(client: T, data: S): S
  def destroyService(client: T, data: S): S

  def liveClient(endpoint: String): ZLayer[Any, Throwable, T]

  val api: ServiceLayerAuto[S, T]
  val manager: ServiceManager[S]
  val live: ULayer[this.type]

  val newApi = new ComputeApi[T]
  val newManager = new ComputeManager[T]



  //def partialDep2: ZLayer[Any, Throwable, T with ServiceLayerAuto[S, T] with ServiceContract.this.type] = null
}



import com.dataintegration.core.automate.services.compute.{ComputeApi, ComputeManager}
import com.dataintegration.core.automate.services.jobsubmit.{JobApi, JobManager}
import com.dataintegration.core.automate.services.storage.{StorageApi, StorageManager}
import com.dataintegration.core.binders.{ComputeConfig, FileStoreConfig, JobConfig}
import com.dataintegration.core.services.configuration.Configuration
import com.dataintegration.core.services.util.{ServiceContract, ServiceLayerAuto}
import com.dataintegration.core.util.{ApplicationLogger, Status}
import zio.{IsNotIntersection, Tag, Task, ULayer, URIO, ZEnv, ZIO, ZIOAppArgs, ZLayer, ZManaged}

object DriverBackup extends zio.ZIOAppDefault with Configuration with ApplicationLogger {

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

    override val manager: ComputeManager[String] = new ComputeManager[String]
    override val api: ComputeApi[String] = new ComputeApi[String]
    override val live: ULayer[ComputeContract.type] = ZLayer.succeed(this)

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

    override val api: ServiceLayerAuto[FileStoreConfig, String] = new StorageApi[String]
    override val manager: StorageManager[String] = new StorageManager[String]
    override val live: ULayer[StorageContract.type] = ZLayer.succeed(this)
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

    override val api: ServiceLayerAuto[JobConfig, String] = new JobApi[String]
    override val manager: JobManager[String] = new JobManager[String]
    override val live: ULayer[JobContract.type] = ZLayer.succeed(this)
  }


  //  val (computeManager, computeApi) = (new ComputeManager[String], new ComputeApi[String])
  //  val (storageManager, storageApi) = (new StorageManager[String], new StorageApi[String])
  //  val (jobManager, jobApi) = (new JobManager[String], new JobApi[String])

  //  def buildDep(contract : ServiceContract[_, _]): ZLayer[Any, Throwable, Any with ServiceLayerAuto[_, _] with contract.type] = {
  //    contract.liveClient("") ++ ZLayer.succeed(contract.api) ++ contract.live2
  //  }

  val endpoint = "us"

  def builDep[T: Tag : IsNotIntersection](contract: ServiceContract[ComputeConfig, T]) = {
    contract.liveClient(endpoint) ++ ZLayer.succeed(contract.api) ++ contract.live
  }

  def buildFullDep[A: Tag : IsNotIntersection, B: Tag : IsNotIntersection, C: Tag : IsNotIntersection](compute: ServiceContract[ComputeConfig, A], storage: ServiceContract[FileStoreConfig, B], job: ServiceContract[JobConfig, C]) = {
    val clusterDependencies =
      (configLayer ++ compute.liveClient(endpoint) ++ ZLayer.succeed(compute.api) ++ compute.live)

    val fileDependencies =
      (configLayer ++ storage.liveClient(endpoint) ++ ZLayer.succeed(storage.api) ++ storage.live)

    val jobDependencies =
      (configLayer ++ job.liveClient(endpoint) ++ ZLayer.succeed(job.api) ++ job.live)

    (clusterDependencies, fileDependencies, jobDependencies)
  }

  val clusterDependencies: ZLayer[Any, Throwable, List[ComputeConfig]] =
    (configLayer ++ ComputeContract.liveClient(endpoint) ++ ZLayer.succeed(ComputeContract.api) ++ ComputeContract.live) >>> ComputeContract.manager.liveManaged

  val clusterDependenciesV2: ZLayer[Any, Throwable, List[ComputeConfig]] =
    (configLayer ++ builDep(ComputeContract)) >>> ComputeContract.manager.liveManaged


  val fileDependencies =
    (configLayer ++ StorageContract.liveClient(endpoint) ++ ZLayer.succeed(StorageContract.api) ++ StorageContract.live) >>> StorageContract.manager.liveManaged

  val jobDependencies =
    (configLayer ++ JobContract.liveClient(endpoint) ++ ZLayer.succeed(JobContract.api) ++ JobContract.live ++ clusterDependenciesV2 ++ fileDependencies) >>> JobContract.manager.live

  val (c1, s1, j1) = buildFullDep(ComputeContract, StorageContract, JobContract)

  val c2 = (configLayer ++ c1) >>> ComputeContract.manager.liveManaged
  val s2 = (configLayer ++ s1) >>> StorageContract.manager.liveManaged
  val j2 = (configLayer ++ j1 ++ c2 ++ s2) >>> JobContract.manager.live

  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] =
    JobContract.manager.Apis.startService.provideLayer(j2)
}
*/