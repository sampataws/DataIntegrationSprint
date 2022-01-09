package com.dataintegration.core.binders

import java.util.UUID

import com.dataintegration.core.services.util.ServiceConfig
import com.dataintegration.core.util.{ApplicationUtils, Status}

case class Job(
                name: String,
                programArguments: Seq[String],
                className: String,
                compute : Cluster = null,
                sparkConf: Map[String, String],
                libraryList: Seq[String],
                status: Status.Type = Status.Pending,
                errorMessage: Seq[String] = Seq.empty
              ) extends ServiceConfig {

  override def getName: String = "SparkJob"

  override val serviceId: String = UUID.randomUUID().toString

  /**
   * Key parameters to print
   *
   * @return
   */
  override def keyParamsToPrint: Map[String, String] = Map(
    "job_name" -> name,
    "class_name" -> className,
    "program_args" -> programArguments.mkString(", "),
    "spark_conf" -> ApplicationUtils.mapToJson(sparkConf),
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
  override def onSuccess(updatedStatus: Status.Type): Job =
    this.copy(status = updatedStatus)

  /**
   * On service Failed - Called when service fails
   *
   * @param failure Failure Type of the service
   * @return
   */
  override def onFailure(updatedStatus: Status.Type)(failure: Throwable): Job = {
    logger.error(failure.printStackTrace().toString)
    this.copy(status = updatedStatus, errorMessage = this.errorMessage :+ failure.getMessage)
  }
}
