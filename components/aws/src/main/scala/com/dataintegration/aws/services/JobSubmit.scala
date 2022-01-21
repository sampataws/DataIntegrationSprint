package com.dataintegration.aws.services

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.elasticmapreduce.model._
import com.amazonaws.services.elasticmapreduce.{AmazonElasticMapReduce, AmazonElasticMapReduceClientBuilder}
import com.dataintegration.core.binders.{JobConfig, Properties}
import com.dataintegration.core.impl.adapter.contracts.JobContract
import com.dataintegration.core.util.Status
import zio.{ULayer, ZLayer}

import scala.jdk.CollectionConverters._

object JobSubmit extends JobContract[AmazonElasticMapReduce] {
  override def createClient(properties: Properties): AmazonElasticMapReduce = {
    val credentialProfile = new ProfileCredentialsProvider("default")
    AmazonElasticMapReduceClientBuilder.standard().withCredentials(credentialProfile).withRegion("").build()
  }

  override def destroyClient(client: AmazonElasticMapReduce) =
    client.shutdown()

  override def createService(client: AmazonElasticMapReduce, data: JobConfig): JobConfig = {

    val sparkConfigs = data.sparkConf.map(conf => new KeyValue(conf._1, conf._2)).toList.asJava
    val args = "spark-submit" +: (data.programArguments ++ data.libraryList)

    val sparkStepConf = new HadoopJarStepConfig()
      //.withJar(data.libraryList.head) // AWS - need to look ???
      .withMainClass(data.className)
      .withProperties(sparkConfigs)
      .withArgs(args.asJava)

    val sparkStep = new StepConfig().withName(data.name + "-" + data.serviceId).withActionOnFailure("CONTINUE").withHadoopJarStep(sparkStepConf)

    val clusterId = data.compute.additionalField1 // extract job flow id
    val req = new AddJobFlowStepsRequest().withJobFlowId(clusterId).withSteps(sparkStep)

    val result: AddJobFlowStepsResult = client.addJobFlowSteps(req)
    val stepId = result.getStepIds.asScala.head

    // describe returns failure details as well - check if we can leverage it
    // https://docs.aws.amazon.com/emr/latest/APIReference/API_DescribeStep.html#API_DescribeStep_ResponseSyntax

    @scala.annotation.tailrec
    def poolStatus(response: DescribeStepResult): JobConfig = {
      val status = response.getStep.getStatus.getState.toUpperCase

      status match {
        case "COMPLETED" => data.copy(status = Status.Success, additionalField1 = response.toString)
        case "FAILED" | "CANCELLED" | "INTERRUPTED" => throw new Exception("Job failed " + response.getStep.toString)
        case _ =>
          Thread.sleep(1000 * 20)
          poolStatus(response)
      }
    }

    val response: DescribeStepResult = client.describeStep(new DescribeStepRequest().withStepId(stepId))
    poolStatus(response)
  }

  override def destroyService(client: AmazonElasticMapReduce, data: JobConfig): JobConfig = data

  override val contractLive: ULayer[JobSubmit.type] = ZLayer.succeed(this)
}
