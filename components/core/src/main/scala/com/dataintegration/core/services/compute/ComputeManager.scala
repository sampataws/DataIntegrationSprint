package com.dataintegration.core.services.compute

import com.dataintegration.core.binders.{Cluster, IntegrationConf}
import com.dataintegration.core.services.util.Service
import zio.{ZIO, ZLayer}

// Backend Layer
trait ComputeManager {
  def spawnCluster: ZIO[Any, Throwable, List[Cluster]]
}

object ComputeManager {
  // Front Api's
  def spawnClusterBackup: ZIO[ComputeManager, Nothing, ZIO[Any, Throwable, List[Cluster]]] =
    ZIO.serviceWith[ComputeManager](_.spawnCluster)

  def spawnClusterV3: ZIO[ComputeManager, Throwable, List[Cluster]] =
    ZIO.environmentWithZIO[ComputeManager](_.get.spawnCluster)

  def spawnClusterV2: ZIO[ComputeManager, Throwable, List[Cluster]] =
    ZIO.serviceWithZIO[ComputeManager](_.spawnCluster)

  val live: ZLayer[Service[Cluster] with IntegrationConf, Nothing, Compute] = {
    for {
      integrationConf <- ZIO.service[IntegrationConf]
      clusterService <- ZIO.service[Service[Cluster]]
    } yield Compute(integrationConf.clusterList, clusterService)
  }.toLayer

  case class Compute(clusterList: List[Cluster], clusterService: Service[Cluster]) extends ComputeManager {
    override def spawnCluster: ZIO[Any, Throwable, List[Cluster]] = for {
      output <- ZIO.foreachPar(clusterList)(clusterService.onCreate).withParallelism(5)
    } yield output
  }

}
