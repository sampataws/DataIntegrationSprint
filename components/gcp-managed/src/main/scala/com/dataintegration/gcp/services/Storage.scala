package com.dataintegration.gcp.services

import java.nio.file.{Files, Paths}

import com.dataintegration.core.binders.{FileStoreConfig, IntegrationConf, Properties}
import com.dataintegration.core.impl.adapter.contracts.StorageContract
import com.dataintegration.core.services.log.audit.DatabaseService
import com.dataintegration.core.util.{ApplicationUtils, Status}
import com.google.cloud.storage.{BlobInfo, CopyWriter, StorageOptions, Storage => GoogleCloudStorage}
import zio.ZLayer

import scala.jdk.CollectionConverters._
//import scala.jdk.CollectionConverters._


object Storage extends StorageContract[GoogleCloudStorage] {

  val className: String = this.getClass.getSimpleName.stripSuffix("$")

  override def createClient(properties: Properties): GoogleCloudStorage = {
    val storage = StorageOptions.newBuilder.build.getService
    logger.info("Storage client created")
    storage
  }

  override def destroyClient(client: GoogleCloudStorage): Unit = {
    logger.info("Storage client is auto closable")
  }

  override def createService(client: GoogleCloudStorage, data: FileStoreConfig): FileStoreConfig = {
    data.sourceBucket.trim.toLowerCase match {
      case "local" => copyLocalToGCS
      case _ => copyGCStoGCS
    }

    def getFullPath(bucket: String, path: String) =
      ApplicationUtils.cleanForwardSlash(bucket + "/" + path)

    def copyLocalToGCS: Boolean = {
      logger.info(s"[$className] Files Copy starting from ${data.sourcePath} to " + getFullPath(data.targetBucket.get, data.targetPath.get))
      val blobInfo = BlobInfo.newBuilder(data.targetBucket.get, data.targetPath.get).build()
      val response = client.create(blobInfo, Files.readAllBytes(Paths.get(data.sourcePath)))
      logger.info(s"[$className] Files Copied from ${data.sourcePath} to " + getFullPath(data.targetBucket.get, data.targetPath.get) +
        s" with Api response ${response.toString}")
      true
    }

    def copyGCStoGCS: Boolean = {
      val blobList = client.list(data.sourceBucket, GoogleCloudStorage.BlobListOption.prefix(data.sourcePath))
      logger.info(s"[$className] Files Copy starting from ${data.sourceBucket}/${data.sourcePath} to " + getFullPath(data.targetBucket.get, data.targetPath.get))
      blobList.iterateAll().asScala.foreach { blob =>
        val copyToPath = ApplicationUtils.cleanForwardSlash(data.targetPath.get + "/" + blob.getName.replace(data.sourcePath,""))
        val res: CopyWriter = blob.copyTo(data.targetBucket.get, copyToPath)

        logger.info(s"[$className] Files Copied from ${data.sourceBucket}/${blob.getName} to " + getFullPath(data.targetBucket.get, copyToPath) +
          s" with Api response ${res.toString}")
      }
      true
    }

    data.copy(status = Status.Success)
  }

  override def destroyService(client: GoogleCloudStorage, data: FileStoreConfig): FileStoreConfig = {
    val blobList = client.list(data.sourceBucket, GoogleCloudStorage.BlobListOption.prefix(data.sourcePath))
    blobList.iterateAll().asScala.foreach { file =>
      val response = file.delete()
      logger.info(s"[$className] Deleted Files " + ApplicationUtils.cleanForwardSlash(file.getBucket + "/" + file.getName) +
        s" with Api response ${response.toString}")
    }
    data.copy(status = Status.Success)
  }

  override def partialDependencies: ZLayer[Any with IntegrationConf with DatabaseService.AuditTableApi, Throwable, List[FileStoreConfig]] =
    (contractLive ++ serviceApiLive ++ clientLive) >>> serviceManager.liveManaged

}
