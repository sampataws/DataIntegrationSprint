package com.dataintegration.demo.impl

import com.dataintegration.core.services.log.audit.TableDefinition
import com.dataintegration.core.util.ApplicationUtils.{cleanForwardSlash, findInArgs}
import com.dataintegration.core.util.Status
import com.dataintegration.database.adapters.ManagedTestSuiteWrapper
import com.dataintegration.demo.job.IndiaDayCount
import com.dataintegration.demo.job.utils.{CompareData, ReadFile}


object IntegrationAppManaged extends ManagedTestSuiteWrapper {

  override def preStep(args: Array[String], featureId: String, workingDir: String): Array[String] = {
    val inputPath = "inputPath=" + cleanForwardSlash(workingDir + "/" + findInArgs(args, "inputPath"))
    val outputPath = "outputPath=" + cleanForwardSlash(workingDir + "/" + findInArgs(args, "outputPath"))
    val expectedOutputPath = "expectedOutput=" + cleanForwardSlash(workingDir + "/" + findInArgs(args, "expectedOutput"))
    val updatedArgs = args.filterNot(_.contains("inputPath")).filterNot(_.contains("outputPath")).filterNot(_.contains("expectedOutput"))
    updatedArgs ++ Array(inputPath, outputPath, expectedOutputPath)
  }

  override def transformationStep(args: Array[String], featureId: String, workingDir: String): Unit =
    IndiaDayCount.main(args)

  override def assertions(value: Seq[TableDefinition.LogScenarios], args: Array[String]): Seq[TableDefinition.LogScenarios] = {
    val actualOutput = ReadFile(findInArgs(args, "outputPath"))
    val expectedOutput = ReadFile(findInArgs(args, "expectedOutput"))
    val assertion = CompareData.data(actualOutput, expectedOutput)
    logger.info("assertion output :- " + assertion)
    if(assertion) value.map(_.copy(status = Status.Success)) else value.map(_.copy(status = Status.Failed))
  }

}
