package com.dataintegration.azure

import com.dataintegration.azure.services._
import com.dataintegration.core.impl.adapter.IntegrationClient
import com.dataintegration.core.services.configuration.ReadConfiguration
import zio.{ZEnv, ZIO, ZIOAppArgs}

object Driver extends IntegrationClient {

  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] = builder(
    configLayer = ReadConfiguration("configPath"),
    computeContract = Compute,
    storageContract = Storage,
    jobContract = JobSubmit
  )
}
