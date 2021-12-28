package com.dataintegration.core.binders

import java.nio.file.{FileSystems, Files, Path}

import com.dataintegration.core.util.ApplicationUtils

import scala.jdk.CollectionConverters._

case class IntegrationConf(
                            clusterList: List[Cluster],
                            featureList: List[Feature],
                            fileStoreList: Map[String, List[FileStore]],
                            properties: Properties) {

  def getClustersList: Seq[Cluster] = clusterList

  def getFilesStoreList: Seq[FileStore] = getJarsToMove ++ getFilesToMove

  def getFeatures: Seq[Feature] =
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
    moveFiles(fileStoreList("jars_to_move"),basePath = properties.workingDir + "/lib/")

  private def moveFiles(listOfFiles : List[FileStore], basePath : String): Seq[FileStore] = listOfFiles.flatMap { fileStore =>
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
  private def moveFilesLocalToCloud(file: FileStore, basePath: String): Seq[FileStore] = {
    val localFiles = filesInDir(file.sourcePath)
    localFiles.map { filePath =>
      file.copy(
        sourcePath = filePath.toString,
        targetBucket = Some(file.targetBucket.getOrElse(file.sourceBucket)),
        targetPath =
          Some(ApplicationUtils.cleanForwardSlash(basePath +
            getTargetPathFromLocal(filePath, file.sourcePath, file.targetPath.getOrElse(""))))
      )
    }
  }

  private def getTargetPathFromLocal(fileName: Path, sourcePath: String, targetPath: String): String = {

    val replaceableString = (sourcePath: String) =>
      if (sourcePath.endsWith("/")) sourcePath
      else {
        val splitFilePath = sourcePath.split("/")
        splitFilePath.slice(0, splitFilePath.length - 1).mkString("/")
      }

    if (targetPath.endsWith("/"))
      targetPath + fileName.toString.replace(replaceableString(sourcePath), "")
    else targetPath
  }

  private def filesInDir(path: String): Seq[Path] = {
    val dirPath = FileSystems.getDefault.getPath(path)
    Files.walk(dirPath).iterator().asScala.filter(Files.isRegularFile(_)).toList
  }

  private def isLocal(path: String) = path.toLowerCase.trim == "local"

}
