package com.dataintegration.core.configuration

import java.io.File

import com.dataintegration.core.binders.IntegrationConf
import zio.config.typesafe.TypesafeConfigSource
import zio.config.{ReadError, read}
import zio.{ZIO, ZLayer}

trait Configuration {
  private val configPath: String = "components\\core\\src\\main\\resources\\integrationTestSuite\\main.conf"

  private lazy val conf =
    TypesafeConfigSource.fromHoconFile(new File(configPath))
      .flatMap(c => read(Descriptors.getIntegrationConf from c))

  @deprecated("Use configLayer")
  protected lazy val readableConf: IntegrationConf = conf match {
    case Left(value) => throw new Exception(value)
    case Right(value) => value
  }

  protected lazy val configLayer: ZLayer[Any, ReadError[String], IntegrationConf] =
    ZIO.fromEither(conf).toLayer
}
