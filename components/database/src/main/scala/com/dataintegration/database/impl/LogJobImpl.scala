package com.dataintegration.database.impl

import com.dataintegration.core.services.log.audit.TableDefinition.LogJob
import com.dataintegration.core.util.ApplicationUtils
import scalikejdbc._

object LogJobImpl extends AuditStructure[LogJob] {

  override val tableName = "dts_job"
  override def primaryKey: scalikejdbc.SQLSyntax = col.jobId
  override def connectionPoolName: String = "default"
  implicit val session: AutoSession.type = AutoSession

  override def createTableStatement: SQL[Nothing, NoExtractor] =
    sql"""CREATE TABLE IF NOT EXISTS $table (
          job_id VARCHAR(36) PRIMARY KEY,
          job_name VARCHAR(36),
          job_type VARCHAR(50),
          config JSON,
          status VARCHAR(10),
          error_message TEXT,
          additional_field1 TEXT,
          created_at TIMESTAMP,
          created_by VARCHAR(30),
          modified_at TIMESTAMP,
          modified_by VARCHAR(30)
          )""".stripMargin

  override def namedValueList(data: LogJob): Seq[(scalikejdbc.SQLSyntax, ParameterBinder)] = Seq(
    col.jobId -> data.jobId,
    col.jobName -> data.jobName,
    col.jobType -> data.jobType,
    col.config -> ApplicationUtils.mapToJson(data.config),
    col.status -> data.status.toString,
    col.errorMessage -> (if(data.errorMessage.isEmpty) null else data.errorMessage.mkString(", ")),
    col.additionalField1 -> data.additionalField1,
    col.createdAt -> data.createdAt,
    col.createdBy -> data.createdBy,
    col.modifiedAt -> data.modifiedAt,
    col.modifiedBy -> data.modifiedBy
  )

  override def updateIntoTable(data: LogJob): SQL[scalikejdbc.UpdateOperation, NoExtractor] =
    updateIntoTable(data, data.jobId)

  override def readTable: Seq[LogJob] =
    sql"select * from $table".map(rs => LogJob(
      jobId = rs.string("job_id"),
      jobName = rs.string("job_name"),
      jobType = rs.string("job_type"),
      config = ApplicationUtils.jsonToMap(rs.string("config")),
      status = stringToStatus(rs.string("status")),
      errorMessage = rs.string("error_message").split(", "),
      additionalField1 = rs.string("additional_field1"),
      createdAt = rs.zonedDateTime("created_at"),
      createdBy = rs.string("created_by"),
      modifiedAt = rs.zonedDateTime("modified_at"),
      modifiedBy = rs.string("modified_by")
    )).list.apply()
}
