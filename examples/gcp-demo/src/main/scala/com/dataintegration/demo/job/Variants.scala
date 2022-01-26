package com.dataintegration.demo.job

import com.dataintegration.core.util.ApplicationUtils
import com.dataintegration.demo.job.rules.VariantCount
import com.dataintegration.demo.job.utils.{ReadFile, WriteFile}

object Variants {

  def main(args: Array[String]): Unit = {
    val inputPath = ApplicationUtils.findInArgs(args, "inputPath")
    val outputPath = ApplicationUtils.findInArgs(args, "outputPath")

    val dataFrame = ReadFile(inputPath)
      .transform(VariantCount.apply)

    WriteFile(dataFrame, outputPath)
  }

}
