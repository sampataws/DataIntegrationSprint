package com.dataintegration.core.binders

import java.util.UUID

import com.dataintegration.core.services.log.audit.TableDefinition
import com.dataintegration.core.services.log.audit.TableDefinition.LogService
import com.dataintegration.core.services.util.ServiceConfig
import com.dataintegration.core.util.{ServiceType, Status}

case class ComputeConfig(
                          serviceId: String = UUID.randomUUID().toString,
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
                          errorMessage: Seq[String],
                          additionalField1: String = null,
                          additionalField2: String = null,
                          additionalField3: String = null
                        ) extends ServiceConfig {

  override def getName: String = s"Cluster :- $clusterName"

  override def getServiceType: ServiceType.Type = ServiceType.Compute

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
  override def onSuccess(updatedStatus: Status.Type): ComputeConfig =
    this.copy(status = updatedStatus)


  /**
   * On service Failed - Called when service fails
   *
   * @param failure Failure Type of the service
   * @return
   */
  override def onFailure(updatedStatus: Status.Type)(failure: Throwable): ComputeConfig = {
    logger.error(failure.printStackTrace().toString)
    this.copy(status = updatedStatus, errorMessage = this.errorMessage :+ failure.getMessage)
  }

}
