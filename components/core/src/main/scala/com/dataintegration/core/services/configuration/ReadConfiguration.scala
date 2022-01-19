package com.dataintegration.core.services.configuration

import java.io.File

import com.dataintegration.core.binders.IntegrationConf
import zio.config.typesafe.TypesafeConfigSource
import zio.config.{ReadError, read}
import zio.{ZIO, ZLayer}

object ReadConfiguration {

  def apply(configPath: String): ZLayer[Any, ReadError[String], IntegrationConf] = {
    val conf =
      TypesafeConfigSource.fromHoconFile(new File(configPath))
        .flatMap(configSource => read(Descriptors.getIntegrationConf from configSource))
    ZIO.fromEither(conf).toLayer
  }
}
