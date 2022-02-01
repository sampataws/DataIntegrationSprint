package com.dataintegration.core.services.configuration

import com.dataintegration.core.binders._

object DescriptorCustomApply {
  def propertiesApply =
    (jobName: String, systemName: String, parentMainClass: String, parentWorkingDir: String, jobArguments: List[String], jobSparkConf: Map[String, String], jarDependencies: List[FileStoreConfig],cloudStoragePrefix : String, maxParallelism: Int, maxRetries: Int, cleanUpFlag: Boolean) =>
      Properties.apply(jobName = jobName,
        systemName = systemName,
        parentMainClass = parentMainClass,
        parentWorkingDir = parentWorkingDir,
        jobArguments = jobArguments,
        jobSparkConf = jobSparkConf,
        jarDependencies = jarDependencies,
        cloudStoragePrefix = cloudStoragePrefix,
        maxParallelism = maxParallelism,
        maxRetries = maxRetries,
        cleanUpFlag = cleanUpFlag)


  def propertiesUnapply
  = (properties: Properties) =>
    Option(properties.jobName,
      properties.systemName,
      properties.parentMainClass,
      properties.parentWorkingDir,
      properties.jobArguments,
      properties.jobSparkConf,
      properties.jarDependencies,
      properties.cloudStoragePrefix,
      properties.maxParallelism,
      properties.maxRetries,
      properties.cleanUpFlag)

  def computeApply =
    (clusterName: String, bucketName: String, project: String, region: String, subnetUri: String, endpoint: String, imageVersion: String, masterMachineTypeUri: String, masterNumInstance: Int, masterBootDiskSizeGB: Int, workerMachineTypeUri: String, workerNumInstance: Int, workerBootDiskSizeGB: Int, idleDeletionDurationSec: Int, weightage: Int) =>
      ComputeConfig.apply(clusterName = clusterName,
        bucketName = bucketName,
        project = project,
        region = region,
        subnetUri = subnetUri,
        endpoint = endpoint,
        imageVersion = imageVersion,
        masterMachineTypeUri = masterMachineTypeUri,
        masterNumInstance = masterNumInstance,
        masterBootDiskSizeGB = masterBootDiskSizeGB,
        workerMachineTypeUri = workerMachineTypeUri,
        workerNumInstance = workerNumInstance,
        workerBootDiskSizeGB = workerBootDiskSizeGB,
        idleDeletionDurationSec = idleDeletionDurationSec,
        weightage = weightage)

  def computeUnApply =
    (compute: ComputeConfig) =>
      Option(compute.clusterName,
        compute.bucketName,
        compute.project,
        compute.region,
        compute.subnetUri,
        compute.endpoint,
        compute.imageVersion,
        compute.masterMachineTypeUri,
        compute.masterNumInstance,
        compute.masterBootDiskSizeGB,
        compute.workerMachineTypeUri,
        compute.workerNumInstance,
        compute.workerBootDiskSizeGB,
        compute.idleDeletionDurationSec,
        compute.weightage)

  def featureApply =
    (name: String, basePath: String, mainClass: Option[String], scenarios: Others.ScenarioConfig, arguments: Option[List[String]], sparkConf: Option[Map[String, String]], executableFlag: Boolean) =>
      Feature.apply(name = name,
        basePath = basePath,
        mainClass = mainClass,
        scenarios = scenarios,
        arguments = arguments,
        sparkConf = sparkConf,
        executableFlag = executableFlag)

  def featureUnApply =
    (feature: Feature) =>
      Option(feature.name,
        feature.basePath,
        feature.mainClass,
        feature.scenarios,
        feature.arguments,
        feature.sparkConf,
        feature.executableFlag)

  def fileStoreApply =
    (sourceBucket: String, sourcePath: String, targetBucket: Option[String], targetPath: Option[String]) =>
      FileStoreConfig.apply(sourceBucket = sourceBucket,
        sourcePath = sourcePath,
        targetBucket = targetBucket,
        targetPath = targetPath)

  def fileStoreUnApply =
    (fileStore: FileStoreConfig) =>
      Option(fileStore.sourceBucket,
        fileStore.sourcePath,
        fileStore.targetBucket,
        fileStore.targetPath)
}
