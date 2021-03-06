package com.dataintegration.core.binders

import java.util.UUID

import com.dataintegration.core.services.util.ServiceConfig
import com.dataintegration.core.util.{ServiceType, Status}

case class JobConfig(
                      serviceId: String = UUID.randomUUID().toString,
                      name: String,
                      programArguments: Seq[String],
                      className: String,
                      compute: ComputeConfig = null,
                      sparkConf: Map[String, String],
                      libraryList: Seq[String],
                      scenarios: Others.ScenarioConfig,
                      status: Status.Type = Status.Pending,
                      errorMessage: Seq[String] = Seq.empty,
                      additionalField1: String = null,
                      additionalField2: String = null,
                      additionalField3: String = null
                    ) extends ServiceConfig {

  override def getName: String = s"SparkJob :- $name"

  override def getServiceId: String = serviceId

  override def getServiceType: ServiceType.Type = ServiceType.JobSubmit

  /**
   * Key parameters to print
   *
   * @return
   */
  override def keyParamsToPrint: Map[String, String] = Map(
    "job_name" -> name,
    "class_name" -> className,
    "program_args" -> programArguments.mkString(", "),
    //"spark_conf" -> ApplicationUtils.mapToJson(sparkConf),
    "libs" -> libraryList.mkString(", ")
  )

  /**
   * Return error message as string
   *
   * @return
   */
  override def getErrorMessage: String = errorMessage.mkString(", ")

  /**
   * Returns status as string
   *
   * @return
   */
  override def getStatus: Status.Type = status

  /**
   * On service success - Called when service completes successfully
   *
   * @return
   */
  override def onSuccess(updatedStatus: Status.Type): JobConfig =
    this.copy(status = updatedStatus)

  /**
   * On service Failed - Called when service fails
   *
   * @param failure Failure Type of the service
   * @return
   */
  override def onFailure(updatedStatus: Status.Type)(failure: Throwable): JobConfig = {
    logger.error(failure.printStackTrace().toString)
    this.copy(status = updatedStatus, errorMessage = this.errorMessage :+ failure.getMessage)
  }
}
