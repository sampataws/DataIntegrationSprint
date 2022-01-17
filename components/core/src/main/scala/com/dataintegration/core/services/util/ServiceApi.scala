package com.dataintegration.core.services.util

import zio.{Task, ZIO}
@deprecated
trait ServiceApi[T] {

  def preJob(): Task[Unit]

  def mainJob: Task[T]

  def postJob(serviceResult: T): Task[Unit]

  def onSuccess: () => T

  def onFailure: Throwable => T

  def retries: Int

  def execute: ZIO[Any, Throwable, T] = for {
    _ <- preJob()
    serviceResult <- mainJob.fold(onFailure, _ => onSuccess())
    _ <- postJob(serviceResult)
  } yield serviceResult

}
