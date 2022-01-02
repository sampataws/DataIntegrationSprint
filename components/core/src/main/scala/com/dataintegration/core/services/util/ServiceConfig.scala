package com.dataintegration.core.services.util

import com.dataintegration.core.util.{ApplicationLogger, Status}
import zio.Task

trait ServiceConfig extends ApplicationLogger {

  def getName: String


  def getServiceId: String

  /**
   * Key parameters to print
   *
   * @return
   */
  def keyParamsToPrint: Map[String, String]

  /**
   * Return error message as string
   *
   * @return
   */
  def getErrorMessage: String

  /**
   * Returns status as string
   *
   * @return
   */
  protected def getStatus: Status.Type

  /**
   * Log Service start on console/DB
   *
   */
  private def logConsoleStart(): Unit =
    logger.info(getLoggingInfo + " Started")

  /**
   * Log Service end on console/DB
   *
   */
  private def logConsoleEnd(): Unit = {
    val errorMessagesIfAny = if (getErrorMessage.isEmpty) "" else "error message :- " + getErrorMessage
    if (getStatus == Status.Failed)
      logger.warn(getLoggingInfo + s" Ended with status ${getStatus} and $errorMessagesIfAny")
    else
      logger.info(getLoggingInfo + s" Ended with status ${getStatus}")
  }

  /**
   * Should insert entry to db table
   *
   * @return
   */
  protected def logAuditStart: String = "Insert into table"

  /**
   * Should update entry which was inserted previously based on serviceId
   * get error message/service_id and status from service itself
   *
   * @return
   */
  protected def logAuditEnd: String = "Update table"

  def logServiceStart: Task[String] = Task {
    logConsoleStart()
    logAuditStart
  }

  def logServiceEnd: Task[String] = Task {
    logConsoleEnd()
    logAuditEnd
  }

  /**
   * On service success - Called when service completes successfully
   *
   * @return
   */
  def onSuccess(updatedStatus: Status.Type): ServiceConfig

  /**
   * On service Failed - Called when service fails
   *
   * @param failure Failure Type of the service
   * @return
   */
  def onFailure(updatedStatus: Status.Type)(failure: Throwable): ServiceConfig

  /**
   * Returns a logging info to print in console
   *
   * @return
   */
  def getLoggingInfo: String = s"Service $getName : $getServiceId with params $mapToString"

  private def mapToString =
    s"{${keyParamsToPrint.map(v => v._1 + " : " + v._2).mkString(",")}}"

}
