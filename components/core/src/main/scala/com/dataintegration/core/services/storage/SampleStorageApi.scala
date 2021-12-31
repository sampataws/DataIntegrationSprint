package com.dataintegration.core.services.storage

import com.dataintegration.core.binders.FileStore
import com.dataintegration.core.services.util.{ServiceLayer, Status}
import zio.Task

object SampleStorageApi extends ServiceLayer[FileStore] {
  override def onCreate(data: FileStore): Task[FileStore] = Task(data.copy(status = Status.Running))

  override def onDestroy(data: FileStore): Task[FileStore] = Task(data.copy(status = Status.Success))

  override def getStatus(data: FileStore): Task[FileStore] = Task(data.copy(status = Status.Failed))
}
