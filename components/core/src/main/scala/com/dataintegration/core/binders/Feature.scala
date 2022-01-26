package com.dataintegration.core.binders

import java.util.UUID

import com.dataintegration.core.util.Status

case class Feature(
                    serviceId: String = UUID.randomUUID().toString,
                    name: String,
                    basePath: String,
                    mainClass: Option[String],
                    scenarios: Others.ScenarioConfig,
                    arguments: Option[List[String]],
                    sparkConf: Option[Map[String, String]],
                    executableFlag: Boolean,
                    status: Status.Type = Status.Pending,
                    errorMessage: Seq[String] = Seq.empty
                  )

