package com.dataintegration.core.services.compute

import com.dataintegration.core.binders.{Cluster, Properties}
import com.dataintegration.core.services.util.{ServiceLayer, ServiceLayerV2, Status}
import zio.Task

object SampleComputeApi extends ServiceLayerV2[Cluster] {

  override def serviceBuilder(task: (Cluster, Properties) => Task[Cluster],
                              service: Cluster,
                              properties: Properties): Task[Cluster] =
    for {
      _ <- service.logServiceStart

      serviceResult <- task(service, properties)
        .retryN(properties.maxClusterRetries)
        .fold(service.onFailure(Status.Failed), _ => service.onSuccess(Status.Success))

      _ <- serviceResult.logServiceEnd
    } yield serviceResult

  override def onCreate(properties: Properties)(data: Cluster): Task[Cluster] =
    serviceBuilderV3(
      task = Task(data.copy(status = Status.Running)),
      service = data,
      onSuccess = data.onSuccess(Status.Success),
      onFailure = data.onFailure(Status.Failed),
      retries = properties.maxClusterRetries
    )

  override def onDestroy(properties: Properties)(data: Cluster): Task[Cluster] =
    serviceBuilderV2(
      task = (data: Cluster, properties: Properties) => Task(data.copy(status = Status.Success)),
      service = data,
      properties = properties,
      onFailure = data.onFailure(Status.Failed),
      onSuccess = data.onSuccess(Status.Success),
      retries = properties.maxClusterRetries
    )

  override def getStatus(properties: Properties)(data: Cluster): Task[Cluster] =
    serviceBuilder(
      task = (data: Cluster, properties: Properties) => Task(data.copy(status = Status.Running)),
      service = data,
      properties = properties
    )

}
