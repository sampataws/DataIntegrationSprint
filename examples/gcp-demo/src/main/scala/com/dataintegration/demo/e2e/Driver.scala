package com.dataintegration.demo.e2e

import com.dataintegration.core.impl.adapter.IntegrationClient
import com.dataintegration.core.services.configuration.{Configuration, ReadConfiguration}
import com.dataintegration.gcp.services.{Compute, JobSubmit, Storage}
import zio.{ZEnv, ZIO, ZIOAppArgs}

object Driver extends IntegrationClient {

  val path = "examples\\gcp-demo\\src\\main\\resources\\integrationTestSuite\\main.conf"

  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] = builder(
    configLayer = ReadConfiguration(path),
    computeContract = Compute,
    storageContract = Storage,
    jobContract = JobSubmit
  )
}
