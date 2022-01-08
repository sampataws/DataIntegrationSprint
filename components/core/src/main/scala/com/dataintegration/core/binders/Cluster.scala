package com.dataintegration.core.binders

import java.util.UUID

import com.dataintegration.core.services.util.ServiceConfig
import com.dataintegration.core.util.Status

case class Cluster(
                    clusterName: String,
                    bucketName: String,
                    project: String,
                    region: String,
                    subnetUri: String,
                    endpoint: String,
                    imageVersion: String,
                    masterMachineTypeUri: String,
                    masterNumInstance: Int,
                    masterBootDiskSizeGB: Int,
                    workerMachineTypeUri: String,
                    workerNumInstance: Int,
                    workerBootDiskSizeGB: Int,
                    idleDeletionDurationSec: Int,
                    weightage: Int,
                    status: Status.Type,
                    errorMessage: Seq[String]
                  ) extends ServiceConfig {

  override def getName: String = "Cluster"

  override val serviceId: String = UUID.randomUUID().toString

  override def keyParamsToPrint: Map[String, String] =
    Map("cluster_name" -> clusterName)

  override def getErrorMessage: String = errorMessage.mkString(", ")

  override def getStatus: Status.Type = status

  /**
   * On service success - Called when service completes successfully
   *
   * @return
   */
  override def onSuccess(updatedStatus: Status.Type): Cluster =
    this.copy(status = updatedStatus)


  /**
   * On service Failed - Called when service fails
   *
   * @param failure Failure Type of the service
   * @return
   */
  override def onFailure(updatedStatus: Status.Type)(failure: Throwable): Cluster = {
    logger.error(failure.printStackTrace().toString)
    this.copy(status = updatedStatus, errorMessage = this.errorMessage :+ failure.getMessage)
  }

}
