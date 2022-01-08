package com.dataintegration.core.services.util

import com.dataintegration.core.util.{ApplicationLogger, Status}

trait ServiceConfig extends ApplicationLogger {

  def getName: String


  val serviceId: String

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
  def getStatus: Status.Type

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
  def getLoggingInfo: String = s"Service $getName : $serviceId with params $mapToString"

  private def mapToString =
    s"{${keyParamsToPrint.map(v => v._1 + " : " + v._2).mkString(",")}}"

}
