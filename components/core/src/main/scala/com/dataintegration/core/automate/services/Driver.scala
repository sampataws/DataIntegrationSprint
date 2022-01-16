package com.dataintegration.core.automate.services

import com.dataintegration.core.automate.adapter.{ComputeContract, JobSubmitContract, ServiceContract, StorageContract}
import com.dataintegration.core.automate.services.Driver.jd
import com.dataintegration.core.binders.{ComputeConfig, FileStoreConfig, JobConfig}
import com.dataintegration.core.services.configuration.Configuration
import com.dataintegration.core.util.{ApplicationLogger, Status}
import zio.{IsNotIntersection, Tag, Task, ULayer, URIO, ZEnv, ZIO, ZIOAppArgs, ZLayer, ZManaged}

object Driver extends zio.ZIOAppDefault with Configuration with ApplicationLogger {

  def debugStr = s"[${Thread.currentThread().getName}] :- "

  object ComputeContract extends ComputeContract[String] {
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

    //override val manager: ComputeManager[String] = new ComputeManager[String]
    //override val api: ComputeApi[String] = new ComputeApi[String]
    override val live: ULayer[ComputeContract.type] = ZLayer.succeed(this)

  }

  object StorageContract extends StorageContract[String] {
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

    //override val api: ServiceLayerAuto[FileStoreConfig, String] = new StorageApi[String]
    //override val manager: StorageManager[String] = new StorageManager[String]
    override val live: ULayer[StorageContract.type] = ZLayer.succeed(this)
  }

  object JobContract extends JobSubmitContract[String] {
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

    //    override val api: ServiceLayerAuto[JobConfig, String] = new JobApi[String]
    //    override val manager: JobManager[String] = new JobManager[String]
    override val live: ULayer[JobContract.type] = ZLayer.succeed(this)
  }

  def jobAppBuilder[A,B,C](
         compute : ComputeContract[A],
         storage : StorageContract[B],
         job : JobSubmitContract[C]): ZIO[Any, Throwable, List[JobConfig]] = {

    val cd = compute.deps(configLayer)
    val sd = storage.deps(configLayer)
    val jd = job.deps(configLayer, cd, sd)
    job.manager.Apis.startService.provideLayer(jd)
  }

  val c2 = ComputeContract.deps(configLayer)
  val s2 = StorageContract.deps(configLayer)
  val jd = JobContract.deps(configLayer, c2, s2)

  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] =
    jobAppBuilder(ComputeContract, StorageContract, JobContract)
  //JobContract.manager.Apis.startService.provideLayer(jd)
}
