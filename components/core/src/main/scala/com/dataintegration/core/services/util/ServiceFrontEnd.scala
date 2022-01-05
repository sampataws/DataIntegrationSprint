package com.dataintegration.core.services.util

import zio.ZIO

trait ServiceFrontEnd[T] {

  trait ServiceBackEnd {
    def startService: ZIO[Any, Throwable, List[T]]
    def stopService: ZIO[Any, Throwable, List[T]]
    def getServiceStatus: ZIO[Any, Throwable, List[T]]
  }

  object Apis {
    def startService: ZIO[ServiceBackEnd, Throwable, List[T]] = ZIO.serviceWithZIO[ServiceBackEnd](_.startService)
    def stopService: ZIO[ServiceBackEnd, Throwable, List[T]] = ZIO.serviceWithZIO[ServiceBackEnd](_.stopService)
    def getServiceStatus: ZIO[ServiceBackEnd, Throwable, List[T]] = ZIO.serviceWithZIO[ServiceBackEnd](_.getServiceStatus)
  }

}
