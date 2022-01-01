package com.dataintegration.core.services.util

import com.dataintegration.core.util.ApplicationLogger
import zio.Task

trait ServiceConfig[T] extends ApplicationLogger {

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
  protected def logConsoleStart(): Unit

  /**
   * Log Service end on console/DB
   *
   */
  protected def logConsoleEnd(service: T): Unit

  /**
   * Should insert entry to db table
   *
   * @return
   */
  protected def logAuditStart(): String = "Insert into table"

  /**
   * Should update entry which was inserted previously based on serviceId
   * get error message/service_id and status from service itself
   *
   * @return
   */
  protected def logAuditEnd(service: T): String = "Update table"

  def logServiceStart(): Task[String] = Task {
    logConsoleStart()
    logAuditStart()
  }

  def logServiceEnd(service : T): Task[String] = Task {
    logConsoleEnd(service)
    logAuditEnd(service)
  }

  /**
   * On service success - Called when service completes successfully
   *
   * @param service Service config type :- Can be compute/feature/job/fileStore
   * @return
   */
  def onSuccess(service: T): T

  /**
   * On service Failed - Called when service fails
   *
   * @param failure Failure Type of the service
   * @return
   */
  def onFailure(failure: Throwable): T

  /**
   * Returns a logging info to print in console
   *
   * @return
   */
  def getLoggingInfo: String = s"Service $getName : $getServiceId with params $mapToString"

  private def mapToString =
    s"{${keyParamsToPrint.map(v => v._1 + " : " + v._2).mkString(",")}}"

}
