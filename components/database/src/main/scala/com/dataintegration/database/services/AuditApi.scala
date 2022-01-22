package com.dataintegration.database.services

import java.util.UUID

import com.dataintegration.core.binders.{ComputeConfig, FileStoreConfig, JobConfig}
import com.dataintegration.core.services.log.audit.{DatabaseService, TableDefinition}
import com.dataintegration.core.services.util.ServiceConfig
import com.dataintegration.database.impl.LogServiceImpl
import scalikejdbc.AutoSession
import zio.{Task, ULayer, ZIO, ZLayer}

import scala.util.Random

object AuditApi extends DatabaseService.AuditTableApi {

  implicit val session = AutoSession

  def randomString(str : String): String = {
    val data = str.split("-").dropRight(1).mkString("-")
    data + "-" +Random.nextInt().toString
  }

  override def insertInDatabase(data: ServiceConfig): Task[Unit] = Task {
    data match {
      case service: ComputeConfig => LogServiceImpl.insertIntoTable(service.getLoggingService).execute.apply()
      case service: FileStoreConfig => LogServiceImpl.insertIntoTable(service.getLoggingService.copy(serviceId = randomString(data.getServiceId),config = Map.empty)).execute.apply()
      case service: JobConfig => LogServiceImpl.insertIntoTable(service.getLoggingService.copy(config = Map.empty)).execute.apply()
    }
  }

  override def updateInDatabase(data: ServiceConfig): Task[Unit] = Task {
    data match {
      case service: ComputeConfig => LogServiceImpl.updateIntoTable(service.getLoggingService).execute.apply()
      case service: FileStoreConfig => ZIO.unit //LogServiceImpl.updateIntoTable(service.getLoggingService.copy(config = Map.empty)).execute.apply()
      case service: JobConfig => LogServiceImpl.updateIntoTable(service.getLoggingService.copy(config = Map.empty)).execute.apply()
    }
  }

  override def insertInDatabase(data: TableDefinition.LogJob): Task[Unit] = ZIO.unit
  override def updateInDatabase(data: TableDefinition.LogJob): Task[Unit] = ZIO.unit
  override def insertInDatabase(data: TableDefinition.LogService): Task[Unit] = ZIO.unit
  override def updateInDatabase(data: TableDefinition.LogService): Task[Unit] = ZIO.unit

  val live: ULayer[AuditApi.type] = ZLayer.succeed(this)

}
