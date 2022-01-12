package com.dataintegration.core.services.utilv2

import com.dataintegration.core.services.util.ServiceConfig
import zio.{Task, ZIO}

trait ServiceApi[OUT <: ServiceConfig, T] {

  def preJob(): Task[Unit]

  def mainJob: Task[T]

  def postJob(serviceResult: ServiceResult[OUT,T]): Task[Unit]

  def onSuccess: T => ServiceResult[OUT,T]

  def onFailure: Throwable => ServiceResult[OUT,T]

  def retries: Int

  def execute: ZIO[Any, Throwable, ServiceResult[OUT,T]] = for {
    _ <- preJob()
    serviceResult <- mainJob.fold(onFailure, onSuccess)
    _ <- postJob(serviceResult)
  } yield serviceResult

}
