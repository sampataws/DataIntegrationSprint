package com.dataintegration.core.services.log.audit

import zio.{Task, ULayer, ZIO, ZLayer}

object DatabaseService {

  trait Service {
    def insertInDatabase(data: TableDefinition.LogJob): Task[Unit]
    def updateInDatabase(data: TableDefinition.LogJob): Task[Unit]
    def insertInDatabase(data: TableDefinition.LogService): Task[Unit]
    def updateInDatabase(data: TableDefinition.LogService): Task[Unit]
  }

  object Apis {
    def insertInDatabase(data: TableDefinition.LogJob): ZIO[Service, Throwable, Unit] =
      ZIO.serviceWithZIO[Service](_.insertInDatabase(data))

    def updateInDatabase(data: TableDefinition.LogJob): ZIO[Service, Throwable, Unit] =
      ZIO.serviceWithZIO[Service](_.updateInDatabase(data))

    def insertInDatabase(data: TableDefinition.LogService): ZIO[Service, Throwable, Unit] =
      ZIO.serviceWithZIO[Service](_.insertInDatabase(data))

    def updateInDatabase(data: TableDefinition.LogService): ZIO[Service, Throwable, Unit] =
      ZIO.serviceWithZIO[Service](_.updateInDatabase(data))
  }

  object NoLog extends Service {
    def tempImpl(text : String) = Task("IN DB " + text).debug.unit
    override def insertInDatabase(data: TableDefinition.LogJob): Task[Unit] = tempImpl("insert LogJob")
    override def updateInDatabase(data: TableDefinition.LogJob): Task[Unit] = tempImpl("update LogJob")
    override def insertInDatabase(data: TableDefinition.LogService): Task[Unit] = tempImpl("insert LogService")
    override def updateInDatabase(data: TableDefinition.LogService): Task[Unit] = tempImpl("insert LogService")
  }

  val live: ULayer[NoLog.type] = ZLayer.succeed(NoLog)

}
