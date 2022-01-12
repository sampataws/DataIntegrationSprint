package com.dataintegration.core.services.compute

import com.dataintegration.core.configuration.Configuration
import com.dataintegration.core.util.{ApplicationLogger, ApplicationUtils}
import zio.{ZEnv, ZIO, ZIOAppArgs, ZLayer}

object Test extends zio.ZIOAppDefault with Configuration with ApplicationLogger {

  val dependencies =
    (ZLayer.succeed(readableConf) ++ ZLayer.succeed(SampleComputeApi)) >>> ComputeManager.live

  def driver = for {
    output <- ComputeManager.Apis.startService.provideLayer(dependencies)
    result = ApplicationUtils.prettyPrintCaseClass(output, logger)
  } yield ()

  def test2 = for {
    output <- SampleComputeApi.onDestroy(readableConf.getProperties)(readableConf.getClustersList.head)
    _ = ApplicationUtils.prettyPrintCaseClass(Seq(output), logger)

  } yield ()

  override def run: ZIO[ZEnv with ZIOAppArgs, Any, Any] = driver
}
