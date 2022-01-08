package com.dataintegration.core.services.job

import com.dataintegration.core.binders.{IntegrationConf, Job, Properties}
import com.dataintegration.core.services.util.{ServiceLayer, ServiceManager}
import zio.{ZIO, ZLayer}

object JobManager extends ServiceManager[Job] {

  val live: ZLayer[IntegrationConf with ServiceLayer[Job], Nothing, JobManagerLive] = {
    for {
      service <- ZIO.service[ServiceLayer[Job]]
      conf <- ZIO.service[IntegrationConf]
    } yield JobManagerLive(service, conf.getJob, conf.getProperties)
  }.toLayer

  val liveManaged: ZLayer[IntegrationConf with ServiceLayer[Job], Throwable, List[Job]] =
    live >>> Apis.startService.toManagedWith(Apis.stopService).toLayer

  /**
   * Todo
   *  - Need dep on running cluster as well
   *  - Need to create driver which has deps on all three so that it doest call on destroy and holds it
   */

  case class JobManagerLive(jobService: ServiceLayer[Job],
                            jobList: List[Job],
                            properties: Properties) extends ServiceBackend {

    override def startService: ZIO[Any, Throwable, List[Job]] = serviceBuilder(
      task = jobService.onCreate(properties),
      listOfResources = jobList,
      failureType = FailSecure,
      parallelism = properties.maxParallelism
    )

    override def stopService(upServices: List[Job]): ZIO[Any, Nothing, List[Job]] = serviceBuilder(
      task = jobService.onDestroy(properties),
      listOfResources = jobList,
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
