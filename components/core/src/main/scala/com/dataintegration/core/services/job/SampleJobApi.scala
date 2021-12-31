package com.dataintegration.core.services.job

import com.dataintegration.core.binders.Job
import com.dataintegration.core.services.util.{ServiceLayer, Status}
import zio.Task

object SampleJobApi extends ServiceLayer[Job] {
  override def onCreate(data: Job): Task[Job] = Task(data.copy(status = Status.Pending))

  override def onDestroy(data: Job): Task[Job] = Task(data.copy(status = Status.Success))

  override def getStatus(data: Job): Task[Job] = Task(data.copy(status = Status.Running))
}
