package com.dataintegration.database.services

import java.time.ZonedDateTime

import com.dataintegration.core.binders.{ComputeConfig, FileStoreConfig, JobConfig, Properties}
import com.dataintegration.core.services.log.audit.{DatabaseServiceV2, TableDefinition}
import com.dataintegration.core.services.util.ServiceConfig
import com.dataintegration.core.util.Status
import com.dataintegration.database.impl.{LogJobImpl, LogServiceImpl}
import scalikejdbc.{DB, NoExtractor, SQL}
import zio.Task

case class AuditApiV2(properties: Properties) extends DatabaseServiceV2.AuditTableApi {

  println("Called AuditApiV2")

  private def sqlQueryWrapper(query: => SQL[scalikejdbc.UpdateOperation, NoExtractor]): Task[Unit] = Task {
    DB localTx { implicit s =>
      // todo - log sql query and map errors
      query.execute.apply()
    }
  }

  private def sqlQueryBuilder(query: TableDefinition.LogService => SQL[scalikejdbc.UpdateOperation, NoExtractor], data: TableDefinition.LogService): Task[Unit] = {
    val updatedData = data.copy(jobId = properties.jobId, modifiedAt = ZonedDateTime.now())
    sqlQueryWrapper(query(updatedData))
  }

  override def insertInDatabase(data: ServiceConfig): Task[Unit] = {
    data match {
      case service: ComputeConfig => sqlQueryBuilder(LogServiceImpl.insertIntoTable, service.getLoggingService)
      case service: FileStoreConfig => sqlQueryBuilder(LogServiceImpl.insertIntoTable, service.getLoggingService)
      case service: JobConfig => sqlQueryBuilder(LogServiceImpl.insertIntoTable, service.getLoggingService)
    }
  }

  override def updateInDatabase(data: ServiceConfig): Task[Unit] = {
    data match {
      case service: ComputeConfig => sqlQueryBuilder(LogServiceImpl.updateIntoTable, service.getLoggingService)
      case service: FileStoreConfig => sqlQueryBuilder(LogServiceImpl.updateIntoTable, service.getLoggingService)
      case service: JobConfig => sqlQueryBuilder(LogServiceImpl.updateIntoTable, service.getLoggingService)
    }
  }

  override def insertInDatabase(): Task[Unit] = {
    val logData = TableDefinition.LogJob(
      jobId = properties.jobId,
      jobName = properties.jobName,
      jobType = "IntegrationTestSuite", // todo
    )
    sqlQueryWrapper(LogJobImpl.insertIntoTable(logData))
  }


  override def updateInDatabase(error: Option[Throwable]): Task[Unit] = {
    val errorMessage = error match {
      case Some(value) => value.toString
      case None => null
    }

    val logData = TableDefinition.LogJob(
      jobId = properties.jobId,
      jobName = properties.jobName,
      jobType = "Test", // todo
      status = if (errorMessage == null) Status.Success else Status.Failed,
      errorMessage = if (errorMessage == null) Seq.empty else Seq(errorMessage),
      modifiedAt = ZonedDateTime.now()
    )
    sqlQueryWrapper(LogJobImpl.updateIntoTable(logData))
  }

}
