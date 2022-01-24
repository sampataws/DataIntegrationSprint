package com.dataintegration.gcp.services.jobsubmit.client

import com.dataintegration.core.util.ApplicationLogger
import com.google.cloud.dataproc.v1.{JobControllerClient, JobControllerSettings}
import zio.{Task, URIO, ZLayer, ZManaged}

object JobClient extends ApplicationLogger {

  def createClient(endpoint: String): Task[JobControllerClient] = Task {
    val jobControllerSettings = JobControllerSettings.newBuilder().setEndpoint(endpoint).build()
    val jobControllerClient = JobControllerClient.create(jobControllerSettings)
    logger.info("Job client initiated")
    jobControllerClient
  }

  def destroyClient(client: JobControllerClient): URIO[Any, Unit] = Task {
    client.shutdown()
    client.close()
    logger.info("Job client destroyed")
  }.orDie

  def live(endpoint: String): ZLayer[Any, Throwable, JobControllerClient] =
    ZManaged.acquireReleaseWith(createClient(endpoint))(destroyClient).toLayer


}
