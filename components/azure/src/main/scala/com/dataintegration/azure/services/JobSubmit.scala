package com.dataintegration.azure.services

import java.io.File

import com.dataintegration.core.binders._
import com.dataintegration.core.impl.adapter.contracts.JobContract
import com.dataintegration.core.services.log.audit.DatabaseService
import com.dataintegration.core.util.Status
import com.microsoft.azure.credentials.ApplicationTokenCredentials
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.implementation.HDInsightManagementClientImpl
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.{ExecuteScriptActionParameters, RuntimeScriptAction}
import zio.ZLayer

import scala.jdk.CollectionConverters._

object JobSubmit extends JobContract[HDInsightManagementClientImpl] {

  override def createClient(properties: Properties): HDInsightManagementClientImpl = {
    val filePath = System.getenv("AZURE_CREDENTIALS")
    val credentials = ApplicationTokenCredentials.fromFile(new File(filePath))
    new HDInsightManagementClientImpl(credentials)
  }

  override def destroyClient(client: HDInsightManagementClientImpl): Unit = Unit

  override def createService(client: HDInsightManagementClientImpl, data: JobConfig): JobConfig = {
    val resourceGroupName = System.getenv("RESOURCE_GROUP_NAME") // todo

    val action = new RuntimeScriptAction().withName(data.name).withUri("Spark submit")
    client.clusters().executeScriptActions(resourceGroupName, data.compute.clusterName, new ExecuteScriptActionParameters().withScriptActions(List(action).asJava))


    @scala.annotation.tailrec
    def poolStatus: JobConfig = {

      val status = client
        .scriptExecutionHistorys()
        .listByCluster(resourceGroupName, data.compute.clusterName).asScala.toList
        .filter(_.name() == data.name).head.status

      status match {
        case "DONE" =>
          data.copy(status = Status.Success)
        case "CANCELLED" | "ERROR" =>
          throw new Exception("")
        case "PENDING" | "RUNNING" =>
          Thread.sleep(1000 * 20)
          poolStatus
        case _ =>
          Thread.sleep(1000 * 30)
          poolStatus
      }

    }

    poolStatus
  }

  override def destroyService(client: HDInsightManagementClientImpl, data: JobConfig): JobConfig = data

  override def partialDependencies: ZLayer[Any with IntegrationConf with DatabaseService.AuditTableApi with List[ComputeConfig] with List[FileStoreConfig], Nothing, JobSubmit.serviceManager.JobLive] =
    (contractLive ++ serviceApiLive ++ clientLive) >>> serviceManager.liveManaged
}
