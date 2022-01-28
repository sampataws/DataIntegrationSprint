package com.dataintegration.demo.e2e

import com.dataintegration.core.binders.IntegrationConf
import com.dataintegration.core.impl.adapter.IntegrationClient
import com.dataintegration.core.services.configuration.ReadConfiguration
import com.dataintegration.core.services.log.audit.DatabaseService
import com.dataintegration.database.services.{AuditApi, CreateConnection}
import com.dataintegration.gcp.services.{Compute, JobSubmit, Storage}
import zio.{ZEnv, ZIO, ZIOAppArgs, ZLayer}

object Driver extends IntegrationClient {

  val path = "examples\\gcp-demo\\src\\main\\resources\\integrationTestSuite\\main.conf"

  override val auditLayer: ZLayer[IntegrationConf, Nothing, DatabaseService.AuditTableApi] = {
    for {
      config <- ZIO.service[IntegrationConf]
    } yield AuditApi(config.getProperties)
  }.toLayer

  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] = builder(
    configLayer = ReadConfiguration(path),
    computeContract = Compute,
    storageContract = Storage,
    jobContract = JobSubmit
  ).provideLayer(CreateConnection.live)
}
