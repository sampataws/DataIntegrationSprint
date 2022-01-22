package com.dataintegration.core.services.log.audit

import com.dataintegration.core.services.util.ServiceConfig
import zio.{Task, ULayer, ZIO, ZLayer}

object DatabaseService {

  trait AuditTableApi {
    def insertInDatabase(data: ServiceConfig): Task[Unit]
    def updateInDatabase(data: ServiceConfig): Task[Unit]
    def insertInDatabase(data: TableDefinition.LogJob): Task[Unit]
    def updateInDatabase(data: TableDefinition.LogJob): Task[Unit]
    @deprecated
    def insertInDatabase(data: TableDefinition.LogService): Task[Unit]
    @deprecated
    def updateInDatabase(data: TableDefinition.LogService): Task[Unit]
  }

  object Apis {
    def insertInDatabase(data: TableDefinition.LogJob): ZIO[AuditTableApi, Throwable, Unit] =
      ZIO.serviceWithZIO[AuditTableApi](_.insertInDatabase(data))

    def updateInDatabase(data: TableDefinition.LogJob): ZIO[AuditTableApi, Throwable, Unit] =
      ZIO.serviceWithZIO[AuditTableApi](_.updateInDatabase(data))

    def insertInDatabase(data: TableDefinition.LogService): ZIO[AuditTableApi, Throwable, Unit] =
      ZIO.serviceWithZIO[AuditTableApi](_.insertInDatabase(data))

    def updateInDatabase(data: TableDefinition.LogService): ZIO[AuditTableApi, Throwable, Unit] =
      ZIO.serviceWithZIO[AuditTableApi](_.updateInDatabase(data))
  }

  object NoLog extends AuditTableApi {
    def tempImpl(text: String) = Task("IN DB " + text).debug.unit
    override def insertInDatabase(data: TableDefinition.LogJob): Task[Unit] = tempImpl("insert LogJob")
    override def updateInDatabase(data: TableDefinition.LogJob): Task[Unit] = tempImpl("update LogJob")
    override def insertInDatabase(data: TableDefinition.LogService): Task[Unit] = tempImpl("insert LogService" + data.serviceName)
    override def updateInDatabase(data: TableDefinition.LogService): Task[Unit] = tempImpl("update LogService" + data.serviceName)
    override def insertInDatabase(data: ServiceConfig): Task[Unit] = tempImpl("with conf insert LogJob")
    override def updateInDatabase(data: ServiceConfig): Task[Unit] = tempImpl("with conf insert LogJob")
  }

  val live: ULayer[NoLog.type] = ZLayer.succeed(NoLog)

}
