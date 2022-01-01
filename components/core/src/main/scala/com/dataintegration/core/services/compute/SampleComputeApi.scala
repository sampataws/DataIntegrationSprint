package com.dataintegration.core.services.compute

import com.dataintegration.core.binders.{Cluster, Properties}
import com.dataintegration.core.services.util.{ServiceLayer, Status}
import zio.Task

object SampleComputeApi extends ServiceLayer[Cluster] {
  override def onCreate(properties: Properties)(data: Cluster): Task[Cluster] = Task(data.copy(status = Status.Running))

  override def onDestroy(properties: Properties)(data: Cluster): Task[Cluster] = Task(data.copy(status = Status.Success))

  override def getStatus(properties: Properties)(data: Cluster): Task[Cluster] = Task(data.copy(status = Status.Success))
}
