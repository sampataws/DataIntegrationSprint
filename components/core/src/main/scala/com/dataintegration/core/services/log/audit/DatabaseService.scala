package com.dataintegration.core.services.log.audit

import com.dataintegration.core.binders.{IntegrationConf, Properties}
import com.dataintegration.core.services.util.ServiceConfig
import zio.{Task, ULayer, ZIO, ZLayer}

object DatabaseService {

  trait AuditTableApi {
    val jobId: String
    def insertInDatabase(data: ServiceConfig): Task[Unit]
    def updateInDatabase(data: ServiceConfig): Task[Unit]
    def insertInDatabase(data: Properties): Task[Unit]
    def updateInDatabase(data: Properties, error: Option[Throwable]): Task[Unit]
  }

  object Apis {
    def insertInDatabase(data: Properties): ZIO[AuditTableApi, Throwable, Unit] =
      ZIO.serviceWithZIO[AuditTableApi](_.insertInDatabase(data))

    def updateInDatabase(data: Properties, error: Option[Throwable]): ZIO[AuditTableApi, Throwable, Unit] =
      ZIO.serviceWithZIO[AuditTableApi](_.updateInDatabase(data, error))

  }

  object NoLog extends AuditTableApi {
    override val jobId: String = null
    def tempImpl(text: String) = Task("IN DB " + text).debug.unit
    override def insertInDatabase(data: Properties): Task[Unit] = tempImpl("insert LogJob")
    override def updateInDatabase(data: Properties, error: Option[Throwable]): Task[Unit] = tempImpl("update LogJob")
    override def insertInDatabase(data: ServiceConfig): Task[Unit] = tempImpl("with conf insert LogJob")
    override def updateInDatabase(data: ServiceConfig): Task[Unit] = tempImpl("with conf insert LogJob")
  }

  val live: ULayer[NoLog.type] = ZLayer.succeed(NoLog)

}
