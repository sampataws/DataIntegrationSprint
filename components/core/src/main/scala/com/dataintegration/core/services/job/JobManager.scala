package com.dataintegration.core.services.job

import com.dataintegration.core.binders._
import com.dataintegration.core.services.util.{ServiceLayer, ServiceManager}
import com.dataintegration.core.util.{ApplicationUtils, Status}
import zio.{ZIO, ZLayer}

object JobManager extends ServiceManager[Job] {

  val live: ZLayer[IntegrationConf with List[FileStore] with List[Cluster] with ServiceLayer[Job], Nothing, JobManagerLive] = {
    for {
      jobService <- ZIO.service[ServiceLayer[Job]]
      clusterList <- ZIO.service[List[Cluster]]
      fileStoreList <- ZIO.service[List[FileStore]]
      conf <- ZIO.service[IntegrationConf]
    } yield JobManagerLive(jobService, conf.getJob, clusterList, fileStoreList, conf.getProperties)
  }.toLayer

  val liveManaged: ZLayer[IntegrationConf with List[FileStore] with List[Cluster] with ServiceLayer[Job], Throwable, List[Job]] =
    live >>> Apis.startService.toManagedWith(Apis.stopService).toLayer

  case class JobManagerLive(jobService: ServiceLayer[Job],
                            jobList: List[Job],
                            clusterList: List[Cluster],
                            fileStoreList: List[FileStore],
                            properties: Properties) extends ServiceBackend {

    // All validations are already done at this point. Not needed as such but there for holding fiber till cluster and file upload is completed
    private def validate =
      fileStoreList.count(_.status == Status.Failed) == 0 && clusterList.count(_.status == Status.Running) > 0

    private def assignJobsToCluster: List[Job] =
      ApplicationUtils.equallyDistributeList(jobList, clusterList.filter(_.status == Status.Running)).map { self =>
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
