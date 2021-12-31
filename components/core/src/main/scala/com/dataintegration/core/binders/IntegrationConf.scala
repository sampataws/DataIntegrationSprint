package com.dataintegration.core.binders

import java.nio.file.{FileSystems, Files, Path}

import com.dataintegration.core.util.{ApplicationLogger, ApplicationUtils}

import scala.jdk.CollectionConverters._

case class IntegrationConf(
                            private val clusterList: List[Cluster],
                            private val featureList: List[Feature],
                            private val fileStoreList: Map[String, List[FileStore]],
                            private val properties: Properties) extends ApplicationLogger {

  def getClustersList: List[Cluster] = clusterList

  def getFileStoreList: List[FileStore] = (getJarsToMove ++ getFilesToMove).toList

  def getFeatures: List[Feature] =
    getExecutableFeatures.map(self => self.copy(
      basePath = ApplicationUtils.cleanForwardSlash(properties.workingDir + self.basePath),
      mainClass = Some(self.mainClass.getOrElse(properties.mainClass)),
      arguments = Some((self.arguments.getOrElse(List.empty) ++ properties.arguments).distinct),
      sparkConf = Some(ApplicationUtils.updateMap(self.sparkConf.getOrElse(Map.empty), properties.sparkConf))))

  def getProperties: Properties = properties

  private def getExecutableFeatures = featureList.filter(_.executableFlag)

  private def getFilesToMove: Seq[FileStore] = {
    for {
      feature <- getFeatures
      fileStore <- fileStoreList
      if feature.name == fileStore._1
    } yield moveFiles(fileStore._2, feature.basePath)
  }.flatten

  private def getJarsToMove: Seq[FileStore] =
    moveFiles(fileStoreList("jars_to_move"), basePath = properties.workingDir)

  private def moveFiles(listOfFiles: List[FileStore], basePath: String): Seq[FileStore] = listOfFiles.flatMap { fileStore =>
    warnFileOrDirectoryStruct(fileStore.sourcePath, fileStore.targetPath.getOrElse(basePath))
    if (isLocal(fileStore.sourceBucket)) moveFilesLocalToCloud(fileStore, basePath)
    else Seq(moveFilesCloudToCloud(fileStore, basePath))
  }

  // cloud to cloud
  private def moveFilesCloudToCloud(file: FileStore, basePath: String): FileStore =
    file.copy(
      targetBucket = Some(file.targetBucket.getOrElse(file.sourceBucket)),
      targetPath = Some(ApplicationUtils.cleanForwardSlash(basePath + file.targetPath.getOrElse("")))
    )

  // local to cloud
  private def moveFilesLocalToCloud(file: FileStore, basePath: String): Seq[FileStore] =
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

    val replaceableString = (sourcePath: String) =>
      if (sourcePath.endsWith(directorySplitter)) sourcePath
      else {
        val splitFilePath = sourcePath.split(directorySplitter)
        splitFilePath.slice(0, splitFilePath.length - 1).mkString("/")
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
