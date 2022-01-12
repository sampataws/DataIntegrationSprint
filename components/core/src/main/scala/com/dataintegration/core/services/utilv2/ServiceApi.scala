package com.dataintegration.core.services.utilv2

import zio.{Task, ZIO}

trait ServiceApi[T] {

  def preJob(): Task[Unit]

  def mainJob: Task[T]

  def postJob(serviceResult: T): Task[Unit]

  def onSuccess[A]: A => T

  def onFailure: Throwable => T

  def retries: Int

  def execute: ZIO[Any, Throwable, T] = for {
    _ <- preJob()
    serviceResult <- mainJob.fold(onFailure, onSuccess)
    _ <- postJob(serviceResult)
  } yield serviceResult

}
