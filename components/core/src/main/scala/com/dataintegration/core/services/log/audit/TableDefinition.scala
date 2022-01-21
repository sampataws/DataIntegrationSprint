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
                     createdBy: String,
                     modifiedAt: ZonedDateTime = ZonedDateTime.now(),
                     modifiedBy: String)

  case class LogService(
                         serviceId: String,
                         jobId: String,
                         serviceName: String,
                         serviceType: String,
                         config: Map[String, String] = Map.empty,
                         status: Status.Type,
                         errorMessage: Seq[String] = Seq.empty,
                         additionalField1: String = null,
                         createdAt: ZonedDateTime = ZonedDateTime.now(),
                         createdBy: String,
                         modifiedAt: ZonedDateTime = ZonedDateTime.now(),
                         modifiedBy: String)


}
