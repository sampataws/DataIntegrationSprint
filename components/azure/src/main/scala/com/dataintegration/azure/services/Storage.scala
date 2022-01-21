package com.dataintegration.azure.services

import java.time.OffsetDateTime

import com.azure.storage.blob.sas.{BlobContainerSasPermission, BlobServiceSasSignatureValues}
import com.azure.storage.blob.{BlobServiceClient, BlobServiceClientBuilder}
import com.dataintegration.core.binders.{FileStoreConfig, Properties}
import com.dataintegration.core.impl.adapter.contracts.StorageContract
import com.dataintegration.core.util.Status
import zio.{ULayer, ZLayer}

import scala.jdk.CollectionConverters._
// https://stackoverflow.com/questions/64870147/how-to-move-file-between-azure-blob-containers-using-java
object Storage extends StorageContract[BlobServiceClient] {

  override def createClient(properties: Properties): BlobServiceClient = {
    val connectStr = System.getenv("AZURE_STORAGE_CONNECTION_STRING")

    val blobServiceClient: BlobServiceClient = new BlobServiceClientBuilder().connectionString(connectStr).buildClient
    val source = blobServiceClient.getBlobContainerClient("")
    val destination = blobServiceClient.getBlobContainerClient("")

    source.listBlobs().asScala.foreach(file => {
      val sourceClient = source.getBlobClient(file.getName)
      val targetClient = destination.getBlobClient(file.getName)


      val sas = new BlobServiceSasSignatureValues(OffsetDateTime.now.plusHours(1), BlobContainerSasPermission.parse("r"))
      val sasToken = sourceClient.generateSas(sas)

      targetClient.beginCopy(sourceClient.getBlobUrl + "?" + sasToken, null)
    })

    ???
  }

  override def destroyClient(client: BlobServiceClient): Unit = Unit

  override def createService(client: BlobServiceClient, data: FileStoreConfig): FileStoreConfig = {
    data.sourceBucket.trim.toLowerCase match {
      case "local" => copyLocalToBlobStorage
      case _ => copyBlobToBlobStorage
    }

    def copyBlobToBlobStorage: Boolean = {
      val source = client.getBlobContainerClient(data.sourceBucket).getBlobClient(data.sourcePath)
      val target = client.getBlobContainerClient(data.targetBucket.get).getBlobClient(data.targetPath.get)
      val sas = new BlobServiceSasSignatureValues(OffsetDateTime.now.plusHours(1), BlobContainerSasPermission.parse("r"))
      val sasToken = source.generateSas(sas)
      target.beginCopy(source.getBlobUrl + "?" + sasToken, null)
      true
    }

    def copyLocalToBlobStorage: Boolean = {
      val target = client.getBlobContainerClient(data.targetBucket.get).getBlobClient(data.targetPath.get)
      target.uploadFromFile(data.sourcePath)
      true
    }

    data.copy(status = Status.Success)
  }

  override def destroyService(client: BlobServiceClient, data: FileStoreConfig): FileStoreConfig = {
    val target = client.getBlobContainerClient(data.targetBucket.get).getBlobClient(data.targetPath.get)
    target.delete()
    data.copy(status = Status.Success)
  }

  override val contractLive: ULayer[Storage.this.type] = ZLayer.succeed(this)
}
