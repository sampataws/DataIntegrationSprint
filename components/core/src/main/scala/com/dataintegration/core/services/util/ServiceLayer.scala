package com.dataintegration.core.services.util

import zio.{ZIO, ZLayer}

trait ServiceLayer[T] {

  trait BackendLayer {
    def onCreate: T
    def onDelete: T
    def getStatus: T
  }

  trait FrontendLayer {
    def onCreate: ZIO[BackendLayer, Nothing, T]
    def onDelete: ZIO[BackendLayer, Nothing, T]
    def getStatus: ZIO[BackendLayer, Nothing, T]
  }

  def createFunctionalDependencies(config: T): BackendLayer

  val live: ZLayer[T, Nothing, BackendLayer]

}
