package com.dataintegration.aws.services

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.elasticmapreduce.model._
import com.amazonaws.services.elasticmapreduce.util.StepFactory
import com.amazonaws.services.elasticmapreduce.{AmazonElasticMapReduce, AmazonElasticMapReduceClientBuilder}
import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.impl.adapter.contracts.ComputeContract
import com.dataintegration.core.util.Status
import zio.{ULayer, ZLayer}

// https://sysadmins.co.za/aws-create-emr-cluster-with-java-sdk-examples/
object ComputeEmr extends ComputeContract[AmazonElasticMapReduce] {

  override def createClient(properties: Properties): AmazonElasticMapReduce = {
    val credentialProfile = new ProfileCredentialsProvider("default")
    AmazonElasticMapReduceClientBuilder.standard().withCredentials(credentialProfile).withRegion("").build()
  }

  override def destroyClient(client: AmazonElasticMapReduce) = {
    client.shutdown()
  }


  override def createService(client: AmazonElasticMapReduce, data: ComputeConfig): ComputeConfig = {
    val stepFactory = new StepFactory()
    val debugging = new StepConfig()
      .withName("Enable debugging")
      .withActionOnFailure("CONTINUE")
      .withHadoopJarStep(stepFactory.newEnableDebuggingStep())

    val jobFlow = new JobFlowInstancesConfig()
      .withEc2SubnetId(data.subnetUri)
      .withEc2KeyName(data.clusterName + "-Ec2")
      .withInstanceCount(data.workerNumInstance)
      .withKeepJobFlowAliveWhenNoSteps(true)
      .withMasterInstanceType(data.masterMachineTypeUri)
      .withSlaveInstanceType(data.workerMachineTypeUri)

    val sparkApplication = new Application().withName("Spark")

    val request = new RunJobFlowRequest()
      .withName(data.clusterName)
      .withReleaseLabel(data.imageVersion) // emr-5.20.0
      .withSteps(debugging)
      .withApplications(sparkApplication)
      .withLogUri(data.bucketName)
      .withServiceRole("EMR_DefaultRole")
      .withJobFlowRole("EMR_EC2_DefaultRole")
      .withInstances(jobFlow)
      .withAutoTerminationPolicy(new AutoTerminationPolicy().withIdleTimeout(data.idleDeletionDurationSec.toLong))
      .withEbsRootVolumeSize(data.masterBootDiskSizeGB)

    val result = client.runJobFlow(request)

    //client.terminateJobFlows()
    data.copy(status = Status.Success, additionalField1 = result.toString)
  }

  override def destroyService(client: AmazonElasticMapReduce, data: ComputeConfig): ComputeConfig = {
    val clusterId = data.additionalField1 // fetch job flow id
    client.terminateJobFlows(new TerminateJobFlowsRequest().withJobFlowIds(clusterId))
    data.copy(status = Status.Success)
  }

  override val contractLive: ULayer[ComputeEmr.type] = ZLayer.succeed(this)
}
