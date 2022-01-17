package com.dataintegration.core.impl.services

import com.dataintegration.core.binders.{ComputeConfig, FileStoreConfig, JobConfig, Properties}
import com.dataintegration.core.impl.adapter.{ComputeContract, JobContract, StorageContract}
import com.dataintegration.core.services.configuration.Configuration
import com.dataintegration.core.util.{ApplicationLogger, Status}
import zio.{ULayer, ZEnv, ZIO, ZIOAppArgs, ZLayer}

object Driver extends zio.ZIOAppDefault with Configuration with ApplicationLogger {

  def debugStr = s"[${Thread.currentThread().getName}] :- "

  object ComputeContract extends ComputeContract[String] {
    override def createClient(properties: Properties): String = properties.jobName
    override def destroyClient(client: String): Unit = "ComputeContract Ended"
    override def createService(client: String, data: ComputeConfig): ComputeConfig = {
      logger.info(s"Cluster create started $client")
      data.copy(status = Status.Running)
    }

    override def destroyService(client: String, data: ComputeConfig): ComputeConfig = {
      logger.info(s"Cluster create done $client")
      data.copy(status = Status.Success)
    }
    //    override def liveClient(endpoint: String): ZLayer[Any, Throwable, String] =
    //      ZManaged.acquireReleaseWith(acquire = createClient("Some"))(release = client => destroyClient(client)).toLayer

    //override val manager: ComputeManager[String] = new ComputeManager[String]
    //override val api: ComputeApi[String] = new ComputeApi[String]

    //override def createClientV2(properties: Properties) : String = properties.jobName

    override val contractLive: ULayer[ComputeContract.type] = ZLayer.succeed(this)

  }

  object StorageContract extends StorageContract[String] {
    override def createClient(properties: Properties): String = properties.jobName
    override def destroyClient(client: String): Unit = "ComputeContract Ended"
    override def createService(client: String, data: FileStoreConfig): FileStoreConfig = {
      logger.info(s"Storage create started $client")
      data.copy(status = Status.Running)
    }
    override def destroyService(client: String, data: FileStoreConfig): FileStoreConfig = {
      logger.info(s"Storage create done $client")
      data.copy(status = Status.Success)
    }
    //    override def liveClient(endpoint: String): ZLayer[Any, Throwable, String] =
    //      ZManaged.acquireReleaseWith(acquire = createClient("Some"))(release = client => destroyClient(client)).toLayer

    //override val api: ServiceLayerAuto[FileStoreConfig, String] = new StorageApi[String]
    //override val manager: StorageManager[String] = new StorageManager[String]
    override val contractLive: ULayer[StorageContract.type] = ZLayer.succeed(this)

  }

  object JobContract extends JobContract[String] {
    override def createClient(properties: Properties): String = properties.jobName
    override def destroyClient(client: String): Unit = "ComputeContract Ended"
    override def createService(client: String, data: JobConfig): JobConfig = {
      logger.info(s"Job create started $client")
      data.copy(status = Status.Running)
    }
    override def destroyService(client: String, data: JobConfig): JobConfig = {
      logger.info(s"Job create done $client")
      data.copy(status = Status.Success)
    }
    //    override def liveClient(endpoint: String): ZLayer[Any, Throwable, String] =
    //      ZManaged.acquireReleaseWith(acquire = createClient("Some"))(release = client => destroyClient(client)).toLayer

    //    override val api: ServiceLayerAuto[JobConfig, String] = new JobApi[String]
    //    override val manager: JobManager[String] = new JobManager[String]
    override val contractLive: ULayer[JobContract.type] = ZLayer.succeed(this)

  }

  def jobAppBuilder[A, B, C](
                              compute: ComputeContract[A],
                              storage: StorageContract[B],
                              job: JobContract[C]): ZIO[Any, Throwable, List[JobConfig]] = {

    val cd = compute.dependencies(configLayer)
    val sd = storage.dependencies(configLayer)
    val jd = job.dependencies(configLayer, cd, sd)
    job.serviceManager.Apis.startService.provideLayer(jd)
  }

  val c2 = ComputeContract.dependencies(configLayer)
  val s2 = StorageContract.dependencies(configLayer)
  val jd = JobContract.dependencies(configLayer, c2, s2)

  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] =
    jobAppBuilder(ComputeContract, StorageContract, JobContract)
  //JobContract.manager.Apis.startService.provideLayer(jd)
}
