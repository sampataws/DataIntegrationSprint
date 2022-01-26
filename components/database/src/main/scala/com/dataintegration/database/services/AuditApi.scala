package com.dataintegration.database.services

import java.time.ZonedDateTime

import com.dataintegration.core.binders.{ComputeConfig, FileStoreConfig, JobConfig, Properties}
import com.dataintegration.core.services.log.audit.{DatabaseService, TableDefinition}
import com.dataintegration.core.services.util.ServiceConfig
import com.dataintegration.core.util.{ApplicationLogger, Status}
import com.dataintegration.database.impl.{LogJobImpl, LogScenarioImpl, LogServiceImpl}
import scalikejdbc.{DB, NoExtractor, SQL}
import zio.Task

case class AuditApi(properties: Properties) extends DatabaseService.AuditTableApi with ApplicationLogger {

  private def sqlQueryWrapper(query: => SQL[scalikejdbc.UpdateOperation, NoExtractor]): Task[Unit] = Task {
    DB localTx { implicit s =>
      logger.info(s"[SQL query] :- ${logSqlStatements(query)}")
      query.execute.apply()
    }
  }.mapError({ exception =>
    logger.error("SQL query failed :- " + exception.printStackTrace())
    exception
  }).unit

  private def logSqlStatements(query: => SQL[scalikejdbc.UpdateOperation, NoExtractor]): String = {
    val statement = query.statement
    val params = query.parameters.map { value =>
      if (value == null) "null" else value.toString
    }

    params.foldLeft(statement) { (text, param) =>
      text.replaceFirst("\\?", param)
    }
  }

  private def sqlQueryBuilder(query: TableDefinition.LogService => SQL[scalikejdbc.UpdateOperation, NoExtractor], data: TableDefinition.LogService): Task[Unit] = {
    val updatedData = data.copy(jobId = properties.jobId, modifiedAt = ZonedDateTime.now())
    sqlQueryWrapper(query(updatedData))
  }

  private def updateLoggingScenarios(service: JobConfig): Task[Unit] = Task {
    DB localTx { implicit s =>
      service.scenarios.getLoggingService.map { self =>
        LogScenarioImpl.insertIntoTable(self.copy(featureId = service.serviceId)).execute.apply()
      }
    }
  }


  override def insertInDatabase(data: ServiceConfig): Task[Unit] = {
    data match {
      case service: ComputeConfig => sqlQueryBuilder(LogServiceImpl.insertIntoTable, service.getLoggingService)
      case service: FileStoreConfig => sqlQueryBuilder(LogServiceImpl.insertIntoTable, service.getLoggingService)
      case service: JobConfig => sqlQueryBuilder(LogServiceImpl.insertIntoTable, service.getLoggingService) *> updateLoggingScenarios(service)
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
