package com.dataintegration.database

import com.dataintegration.core.services.log.audit.TableDefinition.LogService
import com.dataintegration.core.util.{ApplicationUtils, ServiceType, Status}
import scalikejdbc._

object LogServiceImpl extends AuditStructure[LogService] {

  override val tableName = "dts_services"
  override def primaryKey: scalikejdbc.SQLSyntax = col.serviceId
  override def connectionPoolName: String = "default"
  implicit val session: AutoSession.type = AutoSession

  override def createTableStatement: SQL[Nothing, NoExtractor] =
    sql"""CREATE TABLE IF NOT EXISTS $table (
          service_id VARCHAR(36) PRIMARY KEY,
          job_id VARCHAR(36),
          service_name TEXT,
          service_type VARCHAR(50),
          config JSON,
          status VARCHAR(10),
          error_message TEXT,
          additional_field1 TEXT,
          created_at TIMESTAMP,
          created_by VARCHAR(30),
          modified_at TIMESTAMP,
          modified_by VARCHAR(30) )""".stripMargin

  override def namedValueList(data: LogService): Seq[(SQLSyntax, ParameterBinder)] = Seq(
    col.serviceId -> data.serviceId,
    col.jobId -> data.jobId,
    col.serviceName -> data.serviceName,
    col.serviceType -> data.serviceType.toString,
    col.config -> ApplicationUtils.mapToJson(data.config),
    col.status -> data.status.toString,
    col.errorMessage -> data.errorMessage.mkString(", "),
    col.additionalField1 -> data.additionalField1,
    col.createdAt -> data.createdAt,
    col.createdBy -> data.createdBy,
    col.modifiedAt -> data.modifiedAt,
    col.modifiedBy -> data.modifiedBy
  )

  override def updateIntoTable(data: LogService): SQL[scalikejdbc.UpdateOperation, NoExtractor] =
    updateIntoTable(data, data.serviceId)

  override def readTable: Seq[LogService] =
    sql"select * from $table".map(rs => LogService(
      rs.string("service_id"),
      rs.string("job_id"),
      rs.string("service_name"),
      stringToStatusForServiceType(rs.string("service_type")),
      ApplicationUtils.jsonToMap(rs.string("config")),
      stringToStatus(rs.string("status")),
      rs.string("error_message").split(", "),
      rs.string("additional_field1"),
      rs.zonedDateTime("created_at"),
      rs.string("created_by"),
      rs.zonedDateTime("modified_at"),
      rs.string("modified_by")
    )).list.apply()

  // todo generic impl
  private def stringToStatusForServiceType(value: String): ServiceType.Type =
    if (value == ServiceType.Compute.toString) ServiceType.Compute
    else if (value == ServiceType.Storage.toString) ServiceType.Storage
    else if (value == ServiceType.JobSubmit.toString) ServiceType.JobSubmit
    else null
}
