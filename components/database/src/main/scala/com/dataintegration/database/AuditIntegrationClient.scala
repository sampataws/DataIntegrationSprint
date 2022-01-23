package com.dataintegration.database

import com.dataintegration.core.binders.IntegrationConf
import com.dataintegration.core.impl.adapter.IntegrationClientV2
import com.dataintegration.core.impl.services.Driver.{ComputeContract, JobContract, StorageContract}
import com.dataintegration.core.services.configuration.Configuration
import com.dataintegration.core.services.log.audit.DatabaseService
import com.dataintegration.database.services.{AuditApi, CreateConnection}
import zio.{ZEnv, ZIO, ZIOAppArgs, ZLayer}

object AuditIntegrationClient extends IntegrationClientV2 with Configuration {

  override val auditLayer: ZLayer[IntegrationConf, Nothing, DatabaseService.AuditTableApi] = {
    for {
      config <- ZIO.service[IntegrationConf]
    } yield AuditApi(config.getProperties)
  }.toLayer

  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] = builder(
    configLayer = configLayer,
    computeContract = ComputeContract,
    storageContract = StorageContract,
    jobContract = JobContract
  ).provideLayer(CreateConnection.live)
}
