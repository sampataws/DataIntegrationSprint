package com.dataintegration.demo.job

import com.dataintegration.core.util.ApplicationUtils
import com.dataintegration.demo.job.rules.{CountryFilter, DayCount}
import com.dataintegration.demo.job.utils.{ReadFile, WriteFile}

object IndiaDayCount {

  def main(args: Array[String]): Unit = {
    val inputPath = ApplicationUtils.findInArgs(args, "inputPath")
    val outputPath = ApplicationUtils.findInArgs(args, "outputPath")
    val countryFilter = Seq("India")

    val dataFrame = ReadFile(inputPath)
      .transform(CountryFilter(countryFilter))
      .transform(DayCount.apply)

    WriteFile(dataFrame, outputPath)
  }
}
