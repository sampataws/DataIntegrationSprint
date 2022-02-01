package com.dataintegration.database.adapters

import com.dataintegration.core.services.log.audit.TableDefinition.LogScenarios
import com.dataintegration.core.util.{ApplicationLogger, ApplicationUtils, Status}
import com.dataintegration.database.impl.LogScenarioImpl
import com.dataintegration.database.services.CreateConnection

trait ManagedTestSuiteWrapper extends ApplicationLogger {

  def preStep(args: Array[String], featureId: String, workingDir: String): Array[String]

  def transformationStep(args: Array[String], featureId: String, workingDir: String): Unit

  def assertions(value: Seq[LogScenarios],args: Array[String]): Seq[LogScenarios]

  def postStep(args: Array[String]): Unit = ()

  def main(args: Array[String]): Unit = {
    val response = try {
      val featureId = ApplicationUtils.findInArgs(args, "featureId")
      val workingDir = ApplicationUtils.findInArgs(args, "workingDir")

      CreateConnection.createClient
      logger.info(s"Job started with feature id as $featureId and working directory as $workingDir")

      val updatedArguments = preStep(args, featureId, workingDir)
      transformationStep(updatedArguments, featureId, workingDir)

      val assertionsResponse = assertions(LogScenarioImpl.readTable(featureId), updatedArguments)
      LogScenarioImpl.updateIntoTable(assertionsResponse)

      val jobStatus = validateScenarioStatus(assertionsResponse)
      logger.info(s"[Job Validation Status] :- $jobStatus")
      postStep(args)
      jobStatus
    } catch {
      case e: Throwable =>
        logger.error("Job failed with exception \n" + e.printStackTrace())
        false
    }
    finally {
      CreateConnection.destroyClient
    }
    assert(response)
  }

  private def validateScenarioStatus(data: Seq[LogScenarios]): Boolean = {
    val statusToBoolean: Status.Type => Boolean = {
      case Status.Success => true
      case _ => false
    }
    data.map(row => statusToBoolean(row.status)).reduce(_ && _)
  }

}
