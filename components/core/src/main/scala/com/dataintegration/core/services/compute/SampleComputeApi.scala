package com.dataintegration.core.services.compute

import com.dataintegration.core.binders.{Cluster, Properties}
import com.dataintegration.core.services.util.{ServiceLayer, Status}
import zio.Task

object SampleComputeApi extends ServiceLayer[Cluster] {

  override def serviceBuilder(task: (Cluster, Properties) => Task[Cluster], service: Cluster, properties: Properties): Task[Cluster] =
    for {
      _ <- service.logServiceStart

      serviceResult <- task(service, properties)
        .retryN(properties.maxClusterRetries)
        .fold(service.onFailure, _ => service.onSuccess)

      _ <- serviceResult.logServiceEnd
    } yield serviceResult

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
    serviceBuilder(
      task = (data: Cluster, properties: Properties) => Task(data.copy(status = Status.Running)),
      service = data,
      properties = properties
    )

}
