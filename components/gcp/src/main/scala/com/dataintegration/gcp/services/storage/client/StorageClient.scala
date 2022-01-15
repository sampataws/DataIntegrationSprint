package com.dataintegration.gcp.services.storage.client

import com.dataintegration.core.util.ApplicationLogger
import com.google.cloud.storage.{Storage, StorageOptions}
import zio.{Task, ZLayer}

object StorageClient extends ApplicationLogger {

  def createClient: Task[Storage] = Task {
    val storage = StorageOptions.getDefaultInstance.getService
    // StorageOptions.newBuilder.build.getService
    // [Read for auth] https://github.com/GoogleCloudPlatform/java-docs-samples/blob/HEAD/auth/src/main/java/com/google/cloud/auth/samples/AuthExample.java

    logger.info("storage client initiated")
    storage
  }

  val live: ZLayer[Any, Throwable, Storage] =
    createClient.toLayer

}
