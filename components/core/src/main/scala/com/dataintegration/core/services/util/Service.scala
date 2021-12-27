package com.dataintegration.core.services.util

import zio.Task

trait Service[T] {
  def onCreate(data : T) : Task[T]
  def onDestroy(data : T) : Task[T]
  def getStatus(data : T) : Task[T]
}
