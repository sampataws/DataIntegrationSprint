package com.dataintegration.aws.services

import java.io.File

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.dataintegration.core.binders.{FileStoreConfig, Properties}
import com.dataintegration.core.impl.adapter.contracts.StorageContract
import com.dataintegration.core.util.Status
import zio.{ULayer, ZLayer}

import scala.jdk.CollectionConverters._

object Storage extends StorageContract[AmazonS3] {
  override def createClient(properties: Properties): AmazonS3 =
    AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build()

  override def destroyClient(client: AmazonS3): Unit =
    client.shutdown()

  override def createService(client: AmazonS3, data: FileStoreConfig): FileStoreConfig = {
    data.sourceBucket.trim.toLowerCase match {
      case "local" => localToS3
      case _ => S3toS3
    }

    def localToS3: Boolean = {
      client.putObject(data.targetBucket.get, data.targetPath.get, new File(data.sourcePath))
      true
    }

    def S3toS3: Boolean = {
      val fileList = client.listObjects(data.sourceBucket, data.sourcePath).getObjectSummaries.asScala
      fileList.foreach { file =>
        client.copyObject(file.getBucketName, file.getKey, data.targetBucket.get, data.targetPath.get)
      }
      true
    }

    data.copy(status = Status.Success)
  }

  override def destroyService(client: AmazonS3, data: FileStoreConfig): FileStoreConfig = {
    val fileList = client.listObjects(data.targetBucket.get, data.targetPath.get).getObjectSummaries.asScala
    fileList.foreach { file =>
      client.deleteObject(file.getBucketName, file.getKey)
    }

    data.copy(status = Status.Success)
  }

  override val contractLive: ULayer[Storage.type] = ZLayer.succeed(this)
}
