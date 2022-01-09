package com.dataintegration.core.services.job

import com.dataintegration.core.binders.{Cluster, IntegrationConf, Job, Properties}
import com.dataintegration.core.services.util.{ServiceLayer, ServiceManager}
import com.dataintegration.core.util.Status
import zio.{ZIO, ZLayer}

object JobManager extends ServiceManager[Job] {

  val live: ZLayer[IntegrationConf with List[Cluster] with ServiceLayer[Job], Nothing, JobManagerLive] = {
    for {
      service <- ZIO.service[ServiceLayer[Job]]
      clusterList <- ZIO.service[List[Cluster]]
      conf <- ZIO.service[IntegrationConf]
    } yield JobManagerLive(service, conf.getJob, clusterList, conf.getProperties)
  }.toLayer

  val liveManaged: ZLayer[IntegrationConf with List[Cluster] with ServiceLayer[Job], Throwable, List[Job]] =
    live >>> Apis.startService.toManagedWith(Apis.stopService).toLayer

  /**
   * Todo
   *  - Need dep on running cluster as well
   *  - Need to create driver which has deps on all three so that it doest call on destroy and holds it
   */

  case class JobManagerLive(jobService: ServiceLayer[Job],
                            jobList: List[Job],
                            clusterList: List[Cluster],
                            properties: Properties) extends ServiceBackend {

    private def assignJobsToCluster: List[Job] =
      DivideJobsToClusters.equallyDistribute(jobList, clusterList.filter(_.status == Status.Running)).map { self =>
        self._1.copy(compute = self._2)
      }.toList

    override def startService: ZIO[Any, Throwable, List[Job]] = serviceBuilder(
      task = jobService.onCreate(properties),
      listOfResources = assignJobsToCluster,
      failureType = FailSecure,
      parallelism = properties.maxParallelism
    )

    override def stopService(upServices: List[Job]): ZIO[Any, Nothing, List[Job]] = serviceBuilder(
      task = jobService.onDestroy(properties),
      listOfResources = upServices,
      failureType = FailSecure,
      parallelism = properties.maxParallelism
    ).orDie

    override def getServiceStatus: ZIO[Any, Throwable, List[Job]] = serviceBuilder(
      task = jobService.getStatus(properties),
      listOfResources = jobList,
      failureType = FailSecure,
      parallelism = properties.maxParallelism
    )
  }

}
