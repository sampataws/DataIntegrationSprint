package com.dataintegration.gcp.services

import com.dataintegration.core.binders._
import com.dataintegration.core.impl.adapter.contracts.JobContract
import com.dataintegration.core.services.log.audit.DatabaseService
import com.dataintegration.core.util.Status
import com.google.cloud.dataproc.v1.{Job, JobControllerClient, JobControllerSettings, JobPlacement, SparkJob}
import zio.ZLayer

import scala.jdk.CollectionConverters._
//import scala.jdk.CollectionConverters._

object JobSubmit extends JobContract[JobControllerClient] {

  val className: String = this.getClass.getSimpleName.stripSuffix("$")

  override def createClient(properties: Properties): JobControllerClient = {
    // todo config endpoint
    val jobControllerSettings = JobControllerSettings.newBuilder()
      .setEndpoint("us-central1-dataproc.googleapis.com:443").build()

    val jobControllerClient = JobControllerClient.create(jobControllerSettings)
    logger.info("Job client initiated")
    jobControllerClient
  }

  override def destroyClient(client: JobControllerClient): Unit = {
    client.close()
    logger.info("Job client destroyed")
  }

  override def createService(client: JobControllerClient, data: JobConfig): JobConfig = {

    val jobPlacement = JobPlacement.newBuilder().setClusterName(data.compute.clusterName).build()

    // Has to be here as service id gets generated here
    val sparkProperties =
      if (data.sparkConf.contains("spark.app.name")) data.sparkConf
      else data.sparkConf ++ Map("spark.app.name" -> s"${data.name}-${data.serviceId}")

    val sparkJob = SparkJob.newBuilder()
      .setMainClass(data.className)
      .addAllJarFileUris(data.libraryList.asJava)
      .addAllArgs(data.programArguments.asJava)
      .putAllProperties(sparkProperties.asJava)
      .build()

    val jobBuilder = Job.newBuilder().setPlacement(jobPlacement).setSparkJob(sparkJob).build()

    @scala.annotation.tailrec
    def poolStatus(sparkJob: Job): JobConfig = {

      val status = sparkJob.getStatus.getState.toString.toUpperCase

      def loggingText = s"[$className] ${data.name} status :- ${sparkJob.getStatus.getState} with Api response " + sparkJob.getStatus.getDetails

      logger.info(s"[$className] ${data.name} status :- ${sparkJob.getStatus.getState}")

      status match {
        case "DONE" =>
          logger.info(loggingText)
          data.copy(status = Status.Success)
        case "CANCELLED" | "ERROR" =>
          logger.error(loggingText)
          throw new Exception(loggingText)
        case "PENDING" | "RUNNING" =>
          Thread.sleep(1000 * 20)
          poolStatus(sparkJob)
        case _ =>
          logger.debug(loggingText)
          Thread.sleep(1000 * 30)
          poolStatus(sparkJob)
      }
    }

    val response = client.submitJobAsOperationAsync(data.compute.project, data.compute.region, jobBuilder).get()
    poolStatus(response)
  }

  override def destroyService(client: JobControllerClient, data: JobConfig): JobConfig = data

  override def partialDependencies: ZLayer[Any with IntegrationConf with DatabaseService.AuditTableApi with List[ComputeConfig] with List[FileStoreConfig], Nothing, JobSubmit.serviceManager.JobLive] =
    (contractLive ++ serviceApiLive ++ clientLive) >>> serviceManager.live
}
