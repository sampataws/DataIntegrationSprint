package com.dataintegration.core.services.util

import zio.ZIO

trait ServiceFrontLayer[T] {

  trait ServiceBackLayer {
    def onCreate: ZIO[Any, Throwable, List[T]]
    def onDestroy: ZIO[Any, Throwable, List[T]]
    def getStatus: ZIO[Any, Throwable, List[T]]
  }

  object Apis {
    def onCreate: ZIO[ServiceBackLayer, Throwable, List[T]] = ZIO.serviceWithZIO[ServiceBackLayer](_.onCreate)
    def onDestroy: ZIO[ServiceBackLayer, Throwable, List[T]] = ZIO.serviceWithZIO[ServiceBackLayer](_.onDestroy)
    def getStatus: ZIO[ServiceBackLayer, Throwable, List[T]] = ZIO.serviceWithZIO[ServiceBackLayer](_.getStatus)
  }

}
