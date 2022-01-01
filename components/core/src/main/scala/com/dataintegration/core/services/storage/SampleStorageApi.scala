package com.dataintegration.core.services.storage

import com.dataintegration.core.binders.{FileStore, Properties}
import com.dataintegration.core.services.util.{ServiceLayer, Status}
import zio.Task

object SampleStorageApi extends ServiceLayer[FileStore] {
  override def onCreate(properties: Properties)(data: FileStore): Task[FileStore] = Task(data.copy(status = Status.Running))

  override def onDestroy(properties: Properties)(data: FileStore): Task[FileStore] = Task(data.copy(status = Status.Success))

  override def getStatus(properties: Properties)(data: FileStore): Task[FileStore] = Task(data.copy(status = Status.Failed))
}
