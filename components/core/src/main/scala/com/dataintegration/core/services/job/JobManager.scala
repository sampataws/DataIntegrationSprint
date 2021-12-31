package com.dataintegration.core.services.job

import com.dataintegration.core.binders.{IntegrationConf, Job, Properties}
import com.dataintegration.core.services.util.{ServiceFrontEnd, ServiceLayer}
import zio.{Task, ZIO, ZLayer}

object JobManager extends ServiceFrontEnd[Job] {

  val live: ZLayer[IntegrationConf with ServiceLayer[Job], Nothing, Manager] = {
    for {
      service <- ZIO.service[ServiceLayer[Job]]
      integrationConf <- ZIO.service[IntegrationConf]
    } yield Manager(service, integrationConf.getJob, integrationConf.getProperties)
  }.toLayer


  case class Manager(service: ServiceLayer[Job],
                     jobList: List[Job],
                     properties: Properties) extends ServiceBackEnd {


    def builder(task: Job => Task[Job]): ZIO[Any, Throwable, List[Job]] =
      ZIO.foreachPar(jobList)(task).withParallelism(properties.maxJobParallelism)

    override def onCreate: ZIO[Any, Throwable, List[Job]] = builder(service.onCreate)

    override def onDestroy: ZIO[Any, Throwable, List[Job]] = builder(service.onDestroy)

    override def getStatus: ZIO[Any, Throwable, List[Job]] = builder(service.getStatus)
  }

}
