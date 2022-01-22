package com.dataintegration.database

import com.dataintegration.core.util.Status
import scalikejdbc._

trait AuditStructure[T] extends SQLSyntaxSupport[T] {

  val col: ColumnName[T] = column
  def primaryKey : SQLSyntax

  def createTableStatement: SQL[Nothing, NoExtractor]

  def insertIntoTable(data: T): SQL[UpdateOperation, NoExtractor] = withSQL {
    insertInto(this).namedValues(namedValueList(data): _*)
  }

  def updateIntoTable(data: T, id : String): SQL[UpdateOperation, NoExtractor] = withSQL {
    update(this).set(
      namedValueList(data): _*
    ).where.eq(primaryKey, id)
  }

  def updateIntoTable(data: T): SQL[UpdateOperation, NoExtractor]

  def namedValueList(data: T): Seq[(SQLSyntax, ParameterBinder)]

  def readTable: Seq[T]

  def stringToStatus(value: String): Status.Type =
    if (value == Status.Success.toString) Status.Success
    else if (value == Status.Running.toString) Status.Running
    else if (value == Status.Failed.toString) Status.Failed
    else Status.Pending
}
