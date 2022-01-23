package com.dataintegration.core.impl.services

import com.dataintegration.core.binders._
import com.dataintegration.core.impl.adapter.ServiceLayerGenericImpl
import com.dataintegration.core.impl.adapter.contracts.{ComputeContract, JobContract, ServiceContract, StorageContract}
import com.dataintegration.core.impl.services.compute.ComputeApi
import com.dataintegration.core.impl.services.jobsubmit.JobApi
import com.dataintegration.core.impl.services.storage.StorageApi
import com.dataintegration.core.services.configuration.Configuration
import com.dataintegration.core.services.log.audit.DatabaseService
import com.dataintegration.core.services.log.audit.DatabaseService.AuditTableApi
import com.dataintegration.core.util.{ApplicationLogger, ApplicationUtils, Status}
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

    override val contractLive: ULayer[ComputeContract.type] = ZLayer.succeed(this)

    val serviceApi2: ServiceLayerGenericImpl[ComputeConfig,String] = new ComputeApi[String]
    val contractLive2: ULayer[ServiceContract[ComputeConfig,String]] = ZLayer.succeed(this)

   // type PD = Any with IntegrationConf with AuditTableApi

    def partialDependencies: ZLayer[Any with IntegrationConf with AuditTableApi, Throwable, List[ComputeConfig]] =
      (contractLive2 ++ ZLayer.succeed(serviceApi2) ++ clientLive) >>> serviceManager.liveManaged

  }

  object StorageContract extends StorageContract[String] {
    override def createClient(properties: Properties): String = properties.jobName
    override def destroyClient(client: String): Unit = "ComputeContract Ended"
    override def createService(client: String, data: FileStoreConfig): FileStoreConfig = {
      logger.info(s"Storage create started $client + ${data.getName} + ${ApplicationUtils.mapToJson(data.keyParamsToPrint)}")
      data.copy(status = Status.Running)
    }
    override def destroyService(client: String, data: FileStoreConfig): FileStoreConfig = {
      logger.info(s"Storage create done $client + ${data.getName} + ${ApplicationUtils.mapToJson(data.keyParamsToPrint)}")
      data.copy(status = Status.Success)
    }
    override val contractLive: ULayer[StorageContract.type] = ZLayer.succeed(this)

    val serviceApi2: ServiceLayerGenericImpl[FileStoreConfig,String] = new StorageApi[String]
    val contractLive2: ULayer[ServiceContract[FileStoreConfig,String]] = ZLayer.succeed(this)

    //type PD = Any with IntegrationConf with AuditTableApi

    def partialDependencies: ZLayer[Any with IntegrationConf with AuditTableApi, Throwable, List[FileStoreConfig]] =
      (contractLive2 ++ ZLayer.succeed(serviceApi2) ++ clientLive) >>> serviceManager.liveManaged

  }

  object JobContract extends JobContract[String] {
    override def createClient(properties: Properties): String = properties.jobName
    override def destroyClient(client: String): Unit = "ComputeContract Ended"
    override def createService(client: String, data: JobConfig): JobConfig = {
      logger.info(s"Job create started $client " + ApplicationUtils.mapToJson(data.keyParamsToPrint))
      data.copy(status = Status.Running)
    }
    override def destroyService(client: String, data: JobConfig): JobConfig = {
      logger.info(s"Job create done $client" + ApplicationUtils.mapToJson(data.keyParamsToPrint))
      data.copy(status = Status.Success)
    }
    override val contractLive: ULayer[JobContract.type] = ZLayer.succeed(this)

    val serviceApi2: ServiceLayerGenericImpl[JobConfig,String] = new JobApi[String]
    val contractLive2: ULayer[ServiceContract[JobConfig,String]] = ZLayer.succeed(this)

    def partialDependencies: ZLayer[Any with IntegrationConf with AuditTableApi with List[ComputeConfig] with List[FileStoreConfig], Nothing, Driver.JobContract.serviceManager.JobLive] =
      (contractLive2 ++ ZLayer.succeed(serviceApi2) ++ clientLive) >>> JobContract.serviceManager.live


  }

  //val audit: ULayer[AuditTableApi] = DatabaseService.live
  val audit: ZLayer[IntegrationConf, Nothing, AuditTableApi] = DatabaseService.live

  def jobAppBuilder[A, B, C](
                              compute: ComputeContract[A],
                              storage: StorageContract[B],
                              job: JobContract[C]): ZIO[Any, Throwable, List[JobConfig]] = {

    val cd = compute.dependencies(configLayer, audit)
    val sd = storage.dependencies(configLayer, audit)
    val jd = job.dependencies(configLayer,audit, cd, sd)
    job.serviceManager.Apis.startService.provideLayer(jd)
  }


  val hello: ZLayer[Any, Throwable, Driver.JobContract.serviceManager.JobLive] =
    configLayer >>> (audit >>> ((ComputeContract.partialDependencies ++ StorageContract.partialDependencies)  >>> JobContract.partialDependencies))
      //.provideLayer()

  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] =
    //JobContract.serviceManager.Apis.startService.provideLayer(hello)
    jobAppBuilder(ComputeContract, StorageContract, JobContract)
  //JobContract.manager.Apis.startService.provideLayer(jd)
}
