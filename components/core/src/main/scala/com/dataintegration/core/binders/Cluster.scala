package com.dataintegration.core.binders

import com.dataintegration.core.services.util.{ServiceConfig, Status}

case class Cluster(
                    serviceId: String,
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

  override def getName: String = "cluster_creation"

  override def getServiceId: String = serviceId

  override def keyParamsToPrint: Map[String, String] =
    Map("cluster_name" -> clusterName)

  override def getErrorMessage: String = errorMessage.mkString(", ")

  override def getStatus: Status.Type = status

  /**
   * On service success - Called when service completes successfully
   *
   * @return
   */
  override def onSuccess(status: Status.Type): Cluster =
    this.copy(status = Status.Success)


  /**
   * On service Failed - Called when service fails
   *
   * @param failure Failure Type of the service
   * @return
   */
  override def onFailure(status: Status.Type)(failure: Throwable): Cluster =
    this.copy(status = Status.Failed, errorMessage = this.errorMessage :+ failure.getMessage)

}
