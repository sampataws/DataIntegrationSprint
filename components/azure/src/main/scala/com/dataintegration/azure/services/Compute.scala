package com.dataintegration.azure.services

import java.io.File
import java.util.Collections

import com.dataintegration.core.binders.{ComputeConfig, IntegrationConf, Properties}
import com.dataintegration.core.impl.adapter.contracts.ComputeContract
import com.dataintegration.core.services.log.audit.DatabaseService
import com.dataintegration.core.util.Status
import com.microsoft.azure.credentials.ApplicationTokenCredentials
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview._
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.implementation.{ClusterInner, HDInsightManagementClientImpl}
import zio.ZLayer

import scala.jdk.CollectionConverters._

object Compute extends ComputeContract[HDInsightManagementClientImpl] {
  /*
  export AZURE_CREDENTIALS=azure_cred.json
    {
      "clientId": "&lt;client-id&gt;",
      "clientSecret": "&lt;client-key&gt;",
      "subscriptionId": "&lt;subscription-id&gt;",
      "tenantId": "&lt;tenant-id&gt;",
    }
   */
  override def createClient(properties: Properties): HDInsightManagementClientImpl = {
    val filePath = System.getenv("AZURE_CREDENTIALS")
    val credentials = ApplicationTokenCredentials.fromFile(new File(filePath))
    new HDInsightManagementClientImpl(credentials)
  }

  override def destroyClient(client: HDInsightManagementClientImpl): Unit = Unit

  // https://docs.microsoft.com/en-us/java/api/overview/azure/hdinsight?view=azure-java-stable#sdk-installation
  override def createService(client: HDInsightManagementClientImpl, data: ComputeConfig): ComputeConfig = {
    val resourceGroupName = System.getenv("RESOURCE_GROUP_NAME") // todo
    val storageAccountKey = System.getenv("STORAGE_ACCOUNT_KEY") // todo
    val configurations = Map("gateway" -> Map("restAuthCredential.enabled_credential" -> "False"))

    val headNode = new Role()
      .withName(s"${data.clusterName}-head")
      .withTargetInstanceCount(data.masterNumInstance)
      .withHardwareProfile(new HardwareProfile().withVmSize(data.masterMachineTypeUri))
    //.withOsProfile(new OsProfile().withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("admin").withPassword("admin") ))

    val workerNode = new Role()
      .withName(s"${data.clusterName}-worker")
      .withTargetInstanceCount(data.workerNumInstance)
      .withHardwareProfile(new HardwareProfile().withVmSize(data.workerMachineTypeUri))

    val computeProfile = new ComputeProfile().withRoles(List(headNode, workerNode).asJava)

    val storageProfile = new StorageProfile()
      .withStorageaccounts(
        List(new StorageAccount()
          .withName(data.bucketName)
          .withContainer("default")
          .withKey(storageAccountKey)
          .withIsDefault(true)).asJava)

    val clusterProperties = new ClusterCreateProperties()
      .withClusterVersion(data.imageVersion)
      .withOsType(OSType.LINUX)
      .withClusterDefinition(new ClusterDefinition().withKind("spark").withConfigurations(configurations))
      .withTier(Tier.STANDARD)
      .withComputeProfile(computeProfile)
      .withStorageProfile(storageProfile)


    val clusterCreateParametersExtended = new ClusterCreateParametersExtended()
      .withLocation(data.region)
      .withTags(Collections.emptyMap())
      .withProperties(clusterProperties)

    val response: ClusterInner = client.clusters().create(resourceGroupName, data.clusterName, clusterCreateParametersExtended)

    data.copy(status = Status.Running, additionalField1 = response.id())
  }

  override def destroyService(client: HDInsightManagementClientImpl, data: ComputeConfig): ComputeConfig = {
    val resourceGroupName = System.getenv("RESOURCE_GROUP_NAME") // todo
    client.clusters().delete(resourceGroupName, data.clusterName)

    client.clusters().executeScriptActions(resourceGroupName, data.clusterName, new ExecuteScriptActionParameters())

    data.copy(status = Status.Success)
  }

  override def partialDependencies: ZLayer[Any with IntegrationConf with DatabaseService.AuditTableApi, Throwable, List[ComputeConfig]] =
    (contractLive ++ serviceApiLive ++ clientLive) >>> serviceManager.liveManaged
}
