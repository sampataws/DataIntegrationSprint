package com.dataintegration.core.automate.services.jobsubmit

import com.dataintegration.core.binders._
import com.dataintegration.core.services.util.{ServiceContract, ServiceLayerAuto, ServiceManager}
import com.dataintegration.core.util.{ApplicationUtils, Status}
import zio.{IsNotIntersection, Tag, Task, ZIO}

class JobManager[T: Tag : IsNotIntersection] extends ServiceManager[JobConfig] {

  val live = {
    for {
      client <- ZIO.service[T]
      service <- ZIO.service[ServiceLayerAuto[JobConfig, T]]
      contract <- ZIO.service[ServiceContract[JobConfig, T]]
      clusterList <- ZIO.service[List[ComputeConfig]]
      _ <- ZIO.service[List[FileStoreConfig]]
      conf <- ZIO.service[IntegrationConf]
    } yield JobLive(client, service, contract, conf.getJob, clusterList, conf.getProperties)
  }.toLayer


  case class JobLive(
                      client: T,
                      service: ServiceLayerAuto[JobConfig, T],
                      contract: ServiceContract[JobConfig, T],
                      jobList: List[JobConfig],
                      clusterList: List[ComputeConfig],
                      properties: Properties) extends ServiceBackend {

    private def assignJobsToCluster: List[JobConfig] =
      ApplicationUtils.equallyDistributeList(jobList, clusterList.filter(_.status == Status.Running)).map { self =>
        self._1.copy(compute = self._2)
      }.toList

    override def startService: ZIO[Any, Throwable, List[JobConfig]] = serviceBuilder(
      task = service.onCreate(client, contract.createService, properties),
      listOfResources = assignJobsToCluster,
      failureType = FailSecure,
      parallelism = properties.maxParallelism)

    override def stopService(jobList: List[JobConfig]): ZIO[Any, Nothing, List[JobConfig]] = serviceBuilder(
      task = service.onCreate(client, contract.destroyService, properties),
      listOfResources = jobList,
      failureType = FailSecure,
      parallelism = properties.maxParallelism).orDie

    override def getServiceStatus: ZIO[Any, Throwable, List[JobConfig]] = Task(jobList)

  }

}
