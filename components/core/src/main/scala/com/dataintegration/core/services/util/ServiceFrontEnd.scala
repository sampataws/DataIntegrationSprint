package com.dataintegration.core.services.util

import zio.{Task, ZIO, ZManaged}

trait ServiceFrontEnd[T] {

  trait ServiceBackEnd {
    def startService: ZIO[Any, Throwable, List[T]]

    def stopService: ZIO[Any, Nothing, List[T]]

    def getServiceStatus: ZIO[Any, Throwable, List[T]]
  }

  object Apis {
    def startService: ZIO[ServiceBackEnd, Throwable, List[T]] =
      ZIO.serviceWithZIO[ServiceBackEnd](_.startService)

    def stopService: ZIO[ServiceBackEnd, Nothing, List[T]] =
      ZIO.serviceWithZIO[ServiceBackEnd](_.stopService)

    def getServiceStatus: ZIO[ServiceBackEnd, Throwable, List[T]] =
      ZIO.serviceWithZIO[ServiceBackEnd](_.getServiceStatus)

  }

  def serviceBuilder(task: T => Task[T],
                     listOfResources: List[T],
                     parallelism: Int): ZIO[Any, Throwable, List[T]] =
    ZIO.foreachPar(listOfResources)(task).withParallelism(parallelism)


}
