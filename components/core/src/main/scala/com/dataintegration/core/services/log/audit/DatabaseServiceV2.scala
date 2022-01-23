package com.dataintegration.core.services.log.audit

import com.dataintegration.core.binders.{IntegrationConf, Properties}
import com.dataintegration.core.services.util.ServiceConfig
import zio.{Task, ZIO}

object DatabaseServiceV2 {

  trait AuditTableApi {
    val jobId: String
    def insertInDatabase(data: ServiceConfig): Task[Unit]
    def updateInDatabase(data: ServiceConfig): Task[Unit]
    def insertInDatabase(): Task[Unit]
    def updateInDatabase(error: Option[Throwable]): Task[Unit]
  }

  object Apis {
    def insertInDatabase(): ZIO[AuditTableApi, Throwable, Unit] =
      ZIO.serviceWithZIO[AuditTableApi](_.insertInDatabase())

    def updateInDatabase(error: Option[Throwable]): ZIO[AuditTableApi, Throwable, Unit] =
      ZIO.serviceWithZIO[AuditTableApi](_.updateInDatabase(error))

  }

  val live: ZIO[IntegrationConf, Nothing, NoLog] = {
    for {
      config <- ZIO.service[IntegrationConf]
    } yield NoLog(config.getProperties)
  }

  case class NoLog(properties: Properties) extends AuditTableApi {
    override val jobId: String = null
    override def insertInDatabase(data: ServiceConfig): Task[Unit] = ZIO.unit
    override def updateInDatabase(data: ServiceConfig): Task[Unit] = ZIO.unit
    override def insertInDatabase(): Task[Unit] = ZIO.unit
    override def updateInDatabase(error: Option[Throwable]): Task[Unit] = ZIO.unit
  }

}
