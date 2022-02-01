package com.dataintegration.demo.job

import com.dataintegration.core.util.{ApplicationLogger, ApplicationUtils}
import com.dataintegration.demo.job.rules.{CountryFilter, DayCount}
import com.dataintegration.demo.job.utils.{ReadFile, WriteFile}

object IndiaDayCount extends ApplicationLogger {

  def main(args: Array[String]): Unit = {
    val inputPath = ApplicationUtils.findInArgs(args, "inputPath")
    val outputPath = ApplicationUtils.findInArgs(args, "outputPath")
    logger.info(s"Args :- ${args.mkString(",")}")
    val countryFilter = Seq("India")

    val dataFrame = ReadFile(inputPath)
      .transform(CountryFilter(countryFilter))
      .transform(DayCount.apply)

    WriteFile(dataFrame, outputPath)
  }
}
