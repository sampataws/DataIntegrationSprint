package com.dataintegration.core.services.util

import zio.{Task, ZIO}

trait ServiceApi[T <: ServiceConfig] {

  def preJob(): Task[String]

  def mainJob: Task[T]

  def postJob(serviceResult: T): Task[String]

  def onSuccess: () => T

  def onFailure: Throwable => T

  def retries: Int

  def execute: ZIO[Any, Throwable, T] = for {
    _ <- preJob()
    serviceResult <- mainJob.fold(onFailure, _ => onSuccess())
    _ <- postJob(serviceResult)
  } yield serviceResult

}
