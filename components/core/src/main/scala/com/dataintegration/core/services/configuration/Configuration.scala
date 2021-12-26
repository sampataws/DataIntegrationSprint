package com.dataintegration.core.services.configuration

import java.io.File

import com.dataintegration.core.binders.IntegrationConf
import com.dataintegration.core.util.ApplicationUtils
import com.dataintegration.core.util.descriptors.Descriptors
import zio.config._

import zio.config.typesafe.TypesafeConfigSource

object Configuration extends App {
  private val configPath: String = "components\\core\\src\\main\\resources\\integrationTestSuite\\main.conf"

  lazy val conf = TypesafeConfigSource.fromHoconFile(new File(configPath))
    .flatMap(x => read(Descriptors.getIntegrationConf from x))

  lazy val readableConf: IntegrationConf = conf match {
    case Left(ex) => throw new Exception(ex)
    case Right(value) => value
  }

  ApplicationUtils.prettyPrintCaseClass(readableConf.compute)
}
