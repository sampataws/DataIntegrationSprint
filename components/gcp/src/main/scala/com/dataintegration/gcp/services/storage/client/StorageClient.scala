package com.dataintegration.gcp.services.storage.client

import com.dataintegration.core.util.ApplicationLogger
import com.google.cloud.storage.{Storage, StorageOptions}
import zio.{Task, ZLayer}

object StorageClient extends ApplicationLogger {

  def createClient: Task[Storage] = Task {
    val storage = StorageOptions.getDefaultInstance.getService
    logger.info("storage client initiated")
    storage
  }

  val live: ZLayer[Any, Throwable, Storage] =
    createClient.toLayer

}
