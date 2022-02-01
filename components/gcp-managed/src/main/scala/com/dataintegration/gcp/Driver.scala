package com.dataintegration.gcp

import com.dataintegration.core.impl.adapter.IntegrationClient
import com.dataintegration.core.services.configuration.ReadConfiguration
import com.dataintegration.gcp.services.{Compute, JobSubmit, Storage}
import zio.{ZEnv, ZIO, ZIOAppArgs}

object Driver extends IntegrationClient {

  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] = builder(
    configLayer = ReadConfiguration("configPath"),
    computeContract = Compute,
    storageContract = Storage,
    jobContract = JobSubmit
  )
}
