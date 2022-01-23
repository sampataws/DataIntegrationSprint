package com.dataintegration.database.services

import java.time.ZonedDateTime
import java.util.UUID

import com.dataintegration.core.binders.{ComputeConfig, FileStoreConfig, JobConfig, Properties}
import com.dataintegration.core.services.log.audit.{DatabaseService, TableDefinition}
import com.dataintegration.core.services.util.ServiceConfig
import com.dataintegration.core.util.Status
import com.dataintegration.database.impl.{LogJobImpl, LogServiceImpl}
import scalikejdbc.{AutoSession, DB, NoExtractor, SQL}
import zio.{Task, ULayer, ZLayer}

import scala.util.Random

object AuditApi extends DatabaseService.AuditTableApi {

  implicit val session = AutoSession
  val jobId: String = UUID.randomUUID().toString

  def randomString(str: String): String = {
    val data = str.split("-").dropRight(1).mkString("-")
    data + "-" + Random.nextInt().toString
  }

  private def sqlQueryWrapper(query: => SQL[scalikejdbc.UpdateOperation, NoExtractor]): Task[Unit] = Task {
    DB localTx { implicit s =>
      // todo - log sql query and map errors
      query.execute.apply()
    }
  }

  private def sqlQueryBuilder(query: TableDefinition.LogService => SQL[scalikejdbc.UpdateOperation, NoExtractor], data: TableDefinition.LogService): Task[Unit] = {
    val updatedData = data.copy(jobId = jobId, modifiedAt = ZonedDateTime.now())
    sqlQueryWrapper(query(updatedData))
  }

  override def insertInDatabase(data: ServiceConfig): Task[Unit] = {
    data match {
      case service: ComputeConfig => sqlQueryBuilder(LogServiceImpl.insertIntoTable, service.getLoggingService) //LogServiceImpl.insertIntoTable(service.getLoggingService).execute.apply()
      case service: FileStoreConfig => sqlQueryBuilder(LogServiceImpl.insertIntoTable, service.getLoggingService) //LogServiceImpl.insertIntoTable(service.getLoggingService).execute.apply()
      case service: JobConfig => sqlQueryBuilder(LogServiceImpl.insertIntoTable, service.getLoggingService) //LogServiceImpl.insertIntoTable(service.getLoggingService).execute.apply()
    }
  }

  override def updateInDatabase(data: ServiceConfig): Task[Unit] = {
    data match {
      case service: ComputeConfig => sqlQueryBuilder(LogServiceImpl.updateIntoTable, service.getLoggingService) //LogServiceImpl.updateIntoTable(service.getLoggingService).execute.apply()
      case service: FileStoreConfig => sqlQueryBuilder(LogServiceImpl.updateIntoTable, service.getLoggingService) //LogServiceImpl.updateIntoTable(service.getLoggingService).execute.apply()
      case service: JobConfig => sqlQueryBuilder(LogServiceImpl.updateIntoTable, service.getLoggingService) //LogServiceImpl.updateIntoTable(service.getLoggingService).execute.apply()
    }
  }

  override def insertInDatabase(data: Properties): Task[Unit] = {
    val logData = TableDefinition.LogJob(
      jobId = jobId,
      jobName = data.jobName,
      jobType = "IntegrationTestSuite", // todo
    )
    //LogJobImpl.insertIntoTable(logData).execute.apply()
    sqlQueryWrapper(LogJobImpl.insertIntoTable(logData))
  }


  override def updateInDatabase(data: Properties, error: Option[Throwable]): Task[Unit] = {
    val errorMessage = error match {
      case Some(value) => value.toString
      case None => null
    }

    val logData = TableDefinition.LogJob(
      jobId = jobId,
      jobName = data.jobName,
      jobType = "Test", // todo
      status = if (errorMessage == null) Status.Success else Status.Failed,
      errorMessage = if(errorMessage == null) Seq.empty else Seq(errorMessage),
      modifiedAt = ZonedDateTime.now()
    )
    sqlQueryWrapper(LogJobImpl.updateIntoTable(logData))
  }


  val live: ULayer[AuditApi.type] = ZLayer.succeed(this)

}
