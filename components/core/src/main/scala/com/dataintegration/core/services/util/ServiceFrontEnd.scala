package com.dataintegration.core.services.util

import zio.ZIO

trait ServiceFrontEnd[T] {

  trait ServiceBackEnd {
    def onCreate: ZIO[Any, Throwable, List[T]]
    def onDestroy: ZIO[Any, Throwable, List[T]]
    def getStatus: ZIO[Any, Throwable, List[T]]
  }

  object Apis {
    def onCreate: ZIO[ServiceBackEnd, Throwable, List[T]] = ZIO.serviceWithZIO[ServiceBackEnd](_.onCreate)
    def onDestroy: ZIO[ServiceBackEnd, Throwable, List[T]] = ZIO.serviceWithZIO[ServiceBackEnd](_.onDestroy)
    def getStatus: ZIO[ServiceBackEnd, Throwable, List[T]] = ZIO.serviceWithZIO[ServiceBackEnd](_.getStatus)
  }

}
