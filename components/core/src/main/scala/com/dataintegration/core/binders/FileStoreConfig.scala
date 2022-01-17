package com.dataintegration.core.binders

import java.util.UUID

import com.dataintegration.core.services.util.ServiceConfig
import com.dataintegration.core.util.Status

case class FileStoreConfig(
                      serviceId: String = UUID.randomUUID().toString,
                      sourceBucket: String,
                      sourcePath: String,
                      targetBucket: Option[String],
                      targetPath: Option[String],
                      status: Status.Type,
                      errorMessage: Seq[String],
                      additionalField1 : String = null,
                      additionalField2 : String = null,
                      additionalField3 : String = null
                    ) extends ServiceConfig {
  override def getName: String = "FileStore"

  override def getServiceId: String = serviceId

  /**
   * Key parameters to print
   *
   * @return
   */
  override def keyParamsToPrint: Map[String, String] =
    Map("source_path" -> s"$sourceBucket/$sourcePath",
      "target_path" -> s"$getTargetBucket/$getTargetPath"
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
  override def onSuccess(updatedStatus: Status.Type): FileStoreConfig =
    this.copy(status = updatedStatus)

  /**
   * On service Failed - Called when service fails
   *
   * @param failure Failure Type of the service
   * @return
   */
  override def onFailure(updatedStatus: Status.Type)(failure: Throwable): FileStoreConfig = {
    logger.error(failure.printStackTrace().toString)
    this.copy(status = updatedStatus, errorMessage = this.errorMessage :+ failure.getMessage)
  }

  private def getTargetBucket: String = targetBucket.getOrElse(sourceBucket)

  private def getTargetPath: String = targetPath.getOrElse("")
}
