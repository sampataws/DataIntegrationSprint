package com.dataintegration.core.services.compute

import com.dataintegration.core.binders.{Cluster, Properties}
import com.dataintegration.core.services.util.{ServiceConfig, ServiceLayer, Status}
import zio.Task

object SampleComputeApi extends ServiceLayer[Cluster] {

  override def onCreate(properties: Properties)(data: Cluster): Task[Cluster] =
    serviceBuilder(
      task = (data: Cluster, properties: Properties) => Task(data.copy(status = Status.Running)),
      service = data,
      properties = properties
    )

  override def onDestroy(properties: Properties)(data: Cluster): Task[Cluster] =
    serviceBuilder(
      task = (data: Cluster, properties: Properties) => Task(data.copy(status = Status.Success)),
      service = data,
      properties = properties
    )

  override def getStatus(properties: Properties)(data: Cluster): Task[Cluster] =
    for {
      _ <- data.logServiceStart
      serviceResult <- Task(data.copy(status = Status.Success)).fold(data.onFailure, _ => data.onGenericSuccess)
      _ <- serviceResult.logServiceEnd
    } yield (data)

}
