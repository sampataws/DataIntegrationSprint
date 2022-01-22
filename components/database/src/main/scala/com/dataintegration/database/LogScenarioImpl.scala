package com.dataintegration.database

import com.dataintegration.core.services.log.audit.TableDefinition.LogScenarios
import scalikejdbc._

object LogScenarioImpl extends AuditStructure[LogScenarios] {

  override val tableName = "dts_scenarios"
  override def primaryKey: scalikejdbc.SQLSyntax = col.scenarioId
  override def connectionPoolName: String = "default"
  implicit val session: AutoSession.type = AutoSession

  override def createTableStatement: SQL[Nothing, NoExtractor] =
    sql"""CREATE TABLE IF NOT EXISTS $table (
          scenario_id VARCHAR(36) PRIMARY KEY,
          feature_id VARCHAR(36),
          scenario_name TEXT,
          scenario_desc TEXT,
          assertion TEXT,
          status VARCHAR(10),
          error_message TEXT,
          created_at TIMESTAMP,
          created_by VARCHAR(30),
          modified_at TIMESTAMP,
          modified_by VARCHAR(30)
          )""".stripMargin

  override def updateIntoTable(data: LogScenarios): SQL[scalikejdbc.UpdateOperation, NoExtractor] =
    updateIntoTable(data, data.scenarioId)

  override def namedValueList(data: LogScenarios): Seq[(scalikejdbc.SQLSyntax, ParameterBinder)] = Seq(
    col.scenarioId -> data.scenarioId,
    col.featureId -> data.featureId,
    col.scenarioName -> data.scenarioName,
    col.scenarioDesc -> data.scenarioDesc,
    col.assertion -> data.assertion,
    col.status -> data.status.toString,
    col.errorMessage -> data.errorMessage.mkString(", "),
    col.createdAt -> data.createdAt,
    col.createdBy -> data.createdBy,
    col.modifiedAt -> data.modifiedAt,
    col.modifiedBy -> data.modifiedBy
  )

  override def readTable: Seq[LogScenarios] =
    sql"select * from $table".map(rs => LogScenarios(
      scenarioId = rs.string("scenario_id"),
      featureId = rs.string("feature_id"),
      scenarioName = rs.string("scenario_name"),
      scenarioDesc = rs.string("scenario_desc"),
      assertion = rs.string("assertion"),
      status = stringToStatus(rs.string("status")),
      errorMessage = rs.string("error_message").split(", "),
      createdAt = rs.zonedDateTime("created_at"),
      createdBy = rs.string("created_by"),
      modifiedAt = rs.zonedDateTime("modified_at"),
      modifiedBy = rs.string("modified_by")
    )).list.apply()

}
