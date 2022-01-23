package com.dataintegration.core.impl.services.jobsubmit

import com.dataintegration.core.binders._
import com.dataintegration.core.impl.adapter.ServiceLayerGenericImpl
import com.dataintegration.core.impl.adapter.contracts.ServiceContract
import com.dataintegration.core.services.log.audit.{DatabaseServiceV2 => DatabaseService}
import com.dataintegration.core.services.log.audit.DatabaseServiceV2.AuditTableApi
import com.dataintegration.core.services.util.ServiceManager
import com.dataintegration.core.util.{ApplicationUtils, Status}
import zio.{IsNotIntersection, Tag, Task, ZIO}

class JobManager[T: Tag : IsNotIntersection] extends ServiceManager[JobConfig] {

  val live = {
    for {
      client <- ZIO.service[T]
      service <- ZIO.service[ServiceLayerGenericImpl[JobConfig, T]]
      logService <- ZIO.service[DatabaseService.AuditTableApi]
      contract <- ZIO.service[ServiceContract[JobConfig, T]]
      clusterList <- ZIO.service[List[ComputeConfig]]
      _ <- ZIO.service[List[FileStoreConfig]]
      conf <- ZIO.service[IntegrationConf]
    } yield JobLive(client, service, contract, conf.getJob, clusterList, logService, conf.getProperties)
  }.toLayer


  case class JobLive(
                      client: T,
                      service: ServiceLayerGenericImpl[JobConfig, T],
                      contract: ServiceContract[JobConfig, T],
                      jobList: List[JobConfig],
                      clusterList: List[ComputeConfig],
                      auditApi: AuditTableApi,
                      properties: Properties) extends ServiceBackend {

    private def assignJobsToCluster: List[JobConfig] =
      ApplicationUtils.equallyDistributeList(jobList, clusterList.filter(_.status == Status.Running)).map { self =>
        self._1.copy(compute = self._2)
      }.toList

    override def startService: ZIO[Any, Throwable, List[JobConfig]] = serviceBuilder(
      task = service.onCreate(client, contract.createService, auditApi, properties),
      listOfResources = assignJobsToCluster,
      failureType = FailSecure,
      parallelism = properties.maxParallelism)

    override def stopService(jobList: List[JobConfig]): ZIO[Any, Nothing, List[JobConfig]] = serviceBuilder(
      task = service.onCreate(client, contract.destroyService, auditApi, properties),
      listOfResources = jobList,
      failureType = FailSecure,
      parallelism = properties.maxParallelism).orDie

    override def getServiceStatus: ZIO[Any, Throwable, List[JobConfig]] = Task(jobList)

  }

}
