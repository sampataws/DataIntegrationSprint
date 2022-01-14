package com.dataintegration.gcp.services.jobsubmit

import com.dataintegration.core.binders._
import com.dataintegration.core.services.util.{ServiceLayerV2, ServiceManager}
import com.dataintegration.core.util.{ApplicationUtils, Status}
import com.google.cloud.dataproc.v1.JobControllerClient
import zio.{ZIO, ZLayer}

object JobManager extends ServiceManager[JobConfig] {

  val live: ZLayer[IntegrationConf with List[FileStoreConfig] with List[ComputeConfig] with ServiceLayerV2[JobConfig, JobControllerClient] with JobControllerClient, Nothing, JobLive] = {
    for {
      client <- ZIO.service[JobControllerClient]
      service <- ZIO.service[ServiceLayerV2[JobConfig, JobControllerClient]]
      clusterList <- ZIO.service[List[ComputeConfig]]
      _ <- ZIO.service[List[FileStoreConfig]]
      conf <- ZIO.service[IntegrationConf]
    } yield JobLive(client, service, conf.getJob, clusterList, conf.getProperties)
  }.toLayer

  val liveManaged: ZLayer[IntegrationConf with List[FileStoreConfig] with List[ComputeConfig] with ServiceLayerV2[JobConfig, JobControllerClient] with JobControllerClient, Throwable, List[JobConfig]] =
    live >>> Apis.startService.toManagedWith(Apis.stopService).toLayer

  case class JobLive(
                      client: JobControllerClient,
                      service: ServiceLayerV2[JobConfig, JobControllerClient],
                      jobList: List[JobConfig],
                      clusterList: List[ComputeConfig],
                      properties: Properties) extends ServiceBackend {

    private def assignJobsToCluster: List[JobConfig] =
      ApplicationUtils.equallyDistributeList(jobList, clusterList.filter(_.status == Status.Running)).map { self =>
        self._1.copy(compute = self._2)
      }.toList

    override def startService: ZIO[Any, Throwable, List[JobConfig]] = serviceBuilder(
      task = service.onCreate(client, properties),
      listOfResources = assignJobsToCluster,
      failureType = FailSecure,
      parallelism = properties.maxParallelism)

    override def stopService(jobList: List[JobConfig]): ZIO[Any, Nothing, List[JobConfig]] = serviceBuilder(
      task = service.onDestroy(client, properties),
      listOfResources = jobList,
      failureType = FailSecure,
      parallelism = properties.maxParallelism).orDie

    override def getServiceStatus: ZIO[Any, Throwable, List[JobConfig]] = serviceBuilder(
      task = service.getStatus(client, properties),
      listOfResources = jobList,
      failureType = FailSecure,
      parallelism = properties.maxParallelism)
  }

}
