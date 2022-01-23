package com.dataintegration.core.services.util

import com.dataintegration.core.services.log.audit.TableDefinition
import com.dataintegration.core.services.log.audit.TableDefinition.LogService
import com.dataintegration.core.util.{ApplicationLogger, ServiceType, Status}

trait ServiceConfig extends ApplicationLogger {

  def getName: String


  def getServiceId: String

  def getServiceType : ServiceType.Type

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
  def getLoggingInfo: String = s"Service $getName : $getServiceId with params $mapToString"

  def getLoggingService : TableDefinition.LogService = LogService(
    serviceId = getServiceId,
    serviceName = getName,
    serviceType = getServiceType,
    config = keyParamsToPrint,
    status = getStatus,
    errorMessage = if(getErrorMessage.isEmpty) Seq.empty else getErrorMessage.split(", "))

  private def mapToString =
    s"{${keyParamsToPrint.map(v => v._1 + " : " + v._2).mkString(",")}}"

}
