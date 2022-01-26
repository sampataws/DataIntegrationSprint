package com.dataintegration.demo.impl

import com.dataintegration.core.adapters.TestSuiteWrapper
import com.dataintegration.core.util.ApplicationLogger
import com.dataintegration.core.util.ApplicationUtils.{cleanForwardSlash, findInArgs}
import com.dataintegration.demo.job.IndiaDayCount
import com.dataintegration.demo.job.utils.{CompareData, ReadFile}

object IntegrationApp extends TestSuiteWrapper with ApplicationLogger {
  override def preStep(args: Array[String]): Array[String] = {
    val workingDir = findInArgs(args, "workingDir")
    val inputPath = "inputPath=" + cleanForwardSlash(workingDir + "/" + findInArgs(args, "inputPath"))
    val outputPath = "outputPath=" + cleanForwardSlash(workingDir + "/" + findInArgs(args, "outputPath"))
    val expectedOutputPath = "expectedOutput=" + cleanForwardSlash(workingDir + "/" + findInArgs(args, "expectedOutput"))
    val updatedArgs = args.filterNot(_.contains("inputPath")).filterNot(_.contains("outputPath")).filterNot(_.contains("expectedOutput"))
    updatedArgs ++ Array(inputPath, outputPath, expectedOutputPath)
  }

  override def transformationStep(args: Array[String]): Unit =
    IndiaDayCount.main(args)

  override def postStep(args: Array[String]): Unit = {
      val actualOutput = ReadFile(findInArgs(args, "outputPath"))
      val expectedOutput = ReadFile(findInArgs(args, "expectedOutput"))
      val testCaseResult = CompareData.data(actualOutput, expectedOutput)
      logger.info("assertion output :- " + testCaseResult)
      assert(testCaseResult)
  }
}
