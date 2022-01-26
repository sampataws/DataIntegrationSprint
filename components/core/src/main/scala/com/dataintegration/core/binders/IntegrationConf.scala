package com.dataintegration.core.binders

import java.nio.file.{FileSystems, Files, Path}

import com.dataintegration.core.util.{ApplicationLogger, ApplicationUtils}

import scala.jdk.CollectionConverters._

case class IntegrationConf(
                            private val clusterList: List[ComputeConfig],
                            private val featureList: List[Feature],
                            private val properties: Properties) extends ApplicationLogger {

  def getClustersList: List[ComputeConfig] = clusterList

  def getFeatures: List[Feature] = {
    val getBasePath = (feature: Feature) => ApplicationUtils.cleanForwardSlash(properties.parentWorkingDir + feature.basePath)

    getExecutableFeatures.map(feature => feature.copy(
      basePath = getBasePath(feature),
      mainClass = Some(feature.mainClass.getOrElse(properties.parentMainClass)),
      scenarios = feature.scenarios.copy(fileDependencies = moveFiles(feature.scenarios.fileDependencies, getBasePath(feature))),
      arguments = Some((feature.arguments.getOrElse(List.empty) ++ properties.jobArguments).distinct),
      sparkConf = Some(ApplicationUtils.updateMap(feature.sparkConf.getOrElse(Map.empty), properties.jobSparkConf))))
  }

  def getJob: List[JobConfig] =
    getFeatures.map { feature =>
      JobConfig(
        serviceId = feature.serviceId,
        name = feature.name,
        programArguments = feature.arguments.get ++ Seq(s"workingDir=${feature.basePath}",s"featureId=${feature.serviceId}"),
        className = feature.mainClass.get,
        sparkConf = feature.sparkConf.get,
        libraryList = properties.jarDependencies.map(_.targetPath.get),
        scenarios = feature.scenarios
      )
    }

  def getFileStore: List[FileStoreConfig] = getProperties.jarDependencies ++ getFeatures.flatMap(_.scenarios.fileDependencies)

  def getProperties: Properties =
    properties.copy(jarDependencies = moveFiles(properties.jarDependencies, basePath = properties.parentWorkingDir))

  private def getExecutableFeatures = featureList.filter(_.executableFlag)

  private def moveFiles(listOfFiles: List[FileStoreConfig], basePath: String): List[FileStoreConfig] = listOfFiles.flatMap { fileStore =>
    warnFileOrDirectoryStruct(fileStore.sourcePath, fileStore.targetPath.getOrElse(basePath))
    if (isLocal(fileStore.sourceBucket)) moveFilesLocalToCloud(fileStore, basePath)
    else Seq(moveFilesCloudToCloud(fileStore, basePath))
  }

  // cloud to cloud
  private def moveFilesCloudToCloud(file: FileStoreConfig, basePath: String): FileStoreConfig =
    file.copy(
      targetBucket = Some(file.targetBucket.getOrElse(file.sourceBucket)),
      targetPath = Some(ApplicationUtils.cleanForwardSlash(basePath + file.targetPath.getOrElse("")))
    )

  // local to cloud
  private def moveFilesLocalToCloud(file: FileStoreConfig, basePath: String): Seq[FileStoreConfig] =
    filesInDir(file.sourcePath).map { filePath =>
      file.copy(
        sourcePath = filePath.toString,
        targetBucket = Some(file.targetBucket.getOrElse(throw new RuntimeException("" +
          "Target bucket cannot be local. When target bucket is not specified app assumes source bucket as " +
          "target bucket Please explicitly define target bucket in config"))),
        targetPath =
          Some(ApplicationUtils.cleanForwardSlash(basePath +
            getTargetPathFromLocal(filePath, file.sourcePath, file.targetPath.getOrElse(""))))
      )
    }

  private def getTargetPathFromLocal(fileName: Path, sourcePath: String, targetPath: String): String = {
    val directorySplitter = if (System.getProperty("os.name").toLowerCase.contains("windows")) "\\\\" else "/"
    val anotherDirectorySplitterCauseWindows = if (System.getProperty("os.name").toLowerCase.contains("windows")) "\\" else "/"

    val replaceableString = (sourcePath: String) =>
      if (sourcePath.endsWith(directorySplitter)) sourcePath
      else {
        val splitFilePath = sourcePath.split(directorySplitter)
        splitFilePath.slice(0, splitFilePath.length - 1).mkString(anotherDirectorySplitterCauseWindows)
      }

    if (targetPath.endsWith("/") || targetPath.isEmpty)
      targetPath + fileName.toString.replace(replaceableString(sourcePath), "")
    else targetPath
  }.replaceAll("\\\\", "/")

  private def filesInDir(path: String): Seq[Path] = {
    val dirPath = FileSystems.getDefault.getPath(path)
    Files.walk(dirPath).iterator().asScala.filter(Files.isRegularFile(_)).toList
  }

  private def isLocal(path: String) = path.toLowerCase.trim == "local"

  private def warnFileOrDirectoryStruct(source: String, target: String): Unit = {
    val updatedTargetPath = target.split("/")
    if (!source.replaceAll("\\\\", "/").endsWith(if (updatedTargetPath.nonEmpty) updatedTargetPath.last else "/"))
      logger.warn("Make sure when source path and target path are of same type either director or file")
  }

}
