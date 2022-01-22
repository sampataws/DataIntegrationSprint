package com.dataintegration.core.services.log.audit

import java.time.ZonedDateTime

import com.dataintegration.core.util.Status

object TableDefinition {

  case class LogJob(
                     jobId: String,
                     jobName: String,
                     jobType: String,
                     config: Map[String, String] = Map.empty,
                     status: Status.Type,
                     errorMessage: Seq[String] = Seq.empty,
                     additionalField1: String = null,
                     createdAt: ZonedDateTime = ZonedDateTime.now(),
                     createdBy: String = "dts",
                     modifiedAt: ZonedDateTime = ZonedDateTime.now(),
                     modifiedBy: String = "dts")

  case class LogService(
                         serviceId: String,
                         jobId: String = null, // This gets updated on its own while writing - null as a placeholder
                         serviceName: String,
                         serviceType: String,
                         config: Map[String, String] = Map.empty,
                         status: Status.Type,
                         errorMessage: Seq[String] = Seq.empty,
                         additionalField1: String = null,
                         createdAt: ZonedDateTime = ZonedDateTime.now(),
                         createdBy: String = "dts",
                         modifiedAt: ZonedDateTime = ZonedDateTime.now(),
                         modifiedBy: String = "dts")

  case class LogScenarios(
                         scenarioId : String,
                         featureId : String,
                         scenarioName : String,
                         scenarioDesc : String = null,
                         assertion : String,
                         status: Status.Type = Status.Pending,
                         errorMessage: Seq[String] = Seq.empty,
                         createdAt: ZonedDateTime = ZonedDateTime.now(),
                         createdBy: String = "dts",
                         modifiedAt: ZonedDateTime = ZonedDateTime.now(),
                         modifiedBy: String = "dts")

}