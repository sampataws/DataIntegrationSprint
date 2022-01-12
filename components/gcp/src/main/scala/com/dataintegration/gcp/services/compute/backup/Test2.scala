package com.dataintegration.gcp.services.compute.backup

import com.dataintegration.core.binders.{Cluster, IntegrationConf}
import com.dataintegration.core.services.util.ServiceResult
import com.google.cloud.dataproc.v1.ClusterControllerClient
import zio.ZIO

object Test2 {


  trait create[T] {
    def onCreate[T2]: ServiceResult[T,T2]
    def onDestroy[T2](service : ServiceResult[T,T2]): String
    def getStatus[T2](service : ServiceResult[T,T2]): String
  }

  val live = for {
    client <- ZIO.service[ClusterControllerClient]
    initializer <- ZIO.service[create[Cluster]]
    conf <- ZIO.service[IntegrationConf]
  } yield SomeObject(client, initializer, conf)

  case class SomeObject(client: ClusterControllerClient, initializer: create[Cluster], conf: IntegrationConf) extends create[Cluster] {
    override def onCreate[T2]: ServiceResult[Cluster, T2] = ???
    override def onDestroy[T2](service: ServiceResult[Cluster, T2]): String = ???
    override def getStatus[T2](service: ServiceResult[Cluster, T2]): String = ???
  }

  /**
   *
   * ClusterApi.startServiceManaged -> create and delete
   * ClusterApi.startService -> dependant on config
   * ClusterApi.stopService(cluster : Cluster) -> dependant on on create output
   * ClusterApi.getStatus ->  dependant on on create output
   *
   */
}
