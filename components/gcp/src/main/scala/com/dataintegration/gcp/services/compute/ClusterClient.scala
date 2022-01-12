package com.dataintegration.gcp.services.compute

import com.google.cloud.dataproc.v1.{ClusterControllerClient, ClusterControllerSettings}
import zio.{Task, URIO, ZLayer, ZManaged}

object ClusterClient {

  def createClient(endpoint : String): Task[ClusterControllerClient] = Task {
    val clusterControllerSettings = ClusterControllerSettings.newBuilder().setEndpoint(endpoint).build()
    val clusterControllerClient = ClusterControllerClient.create(clusterControllerSettings)
    clusterControllerClient
  }

  def destroyClient(client : ClusterControllerClient): URIO[Any, Unit] = Task {
    client.shutdown() // todo :- Need to check
    client.close()
  }.orDie


  def live(endpoint : String): ZLayer[Any, Throwable, ClusterControllerClient] =
    ZManaged.acquireReleaseWith(acquire = createClient(endpoint))(release = client => destroyClient(client)).toLayer

}
