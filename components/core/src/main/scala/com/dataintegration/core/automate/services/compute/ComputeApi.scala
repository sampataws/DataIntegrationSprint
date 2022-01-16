package com.dataintegration.core.automate.services.compute

import com.dataintegration.core.automate.services.compute.application.{CreateCluster, DeleteCluster}
import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.dataintegration.core.services.util.ServiceLayerAuto
import zio.{Task, ULayer, ZLayer}

class ComputeApi[T] extends ServiceLayerAuto[ComputeConfig, T] {

  override def onCreate(
                         client: T,
                         job: (T, ComputeConfig) => ComputeConfig,
                         properties: Properties)(data: ComputeConfig): Task[ComputeConfig] =
    CreateCluster(client, data, job, properties).execute

  override def onDestroy(
                          client: T,
                          job: (T, ComputeConfig) => ComputeConfig,
                          properties: Properties)(data: ComputeConfig): Task[ComputeConfig] =
    DeleteCluster(client, data, job, properties).execute
}
