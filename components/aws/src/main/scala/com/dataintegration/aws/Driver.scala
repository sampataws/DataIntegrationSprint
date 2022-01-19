package com.dataintegration.aws

import com.dataintegration.aws.services.{ComputeEmr, JobSubmitSparkEmr, StorageS3}
import com.dataintegration.core.impl.adapter.IntegrationClient
import com.dataintegration.core.services.configuration.ReadConfiguration
import zio.{ZEnv, ZIO, ZIOAppArgs}


object Driver extends IntegrationClient {

  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] = builder(
    configLayer = ReadConfiguration("configPath"),
    computeContract = ComputeEmr,
    storageContract = StorageS3,
    jobContract = JobSubmitSparkEmr
  )
}
