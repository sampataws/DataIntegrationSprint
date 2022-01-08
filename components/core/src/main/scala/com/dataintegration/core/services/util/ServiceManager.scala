package com.dataintegration.core.services.util

import zio.{Task, ZIO}

trait ServiceManager[T] {

  trait ServiceBackend {
    def startService: ZIO[Any, Throwable, List[T]]

    def stopService(upServices : List[T]): ZIO[Any, Nothing, List[T]]

    def getServiceStatus: ZIO[Any, Throwable, List[T]]
  }

  object Apis {
    def startService: ZIO[ServiceBackend, Throwable, List[T]] =
      ZIO.serviceWithZIO[ServiceBackend](_.startService)

    def stopService(upServices : List[T]): ZIO[ServiceBackend, Nothing, List[T]] =
      ZIO.serviceWithZIO[ServiceBackend](_.stopService(upServices))

    def getServiceStatus: ZIO[ServiceBackend, Throwable, List[T]] =
      ZIO.serviceWithZIO[ServiceBackend](_.getServiceStatus)

  }

  def serviceBuilder(task: T => Task[T],
                     listOfResources: List[T],
                     parallelism: Int): ZIO[Any, Throwable, List[T]] =
    ZIO.foreachPar(listOfResources)(task).withParallelism(parallelism)


}
