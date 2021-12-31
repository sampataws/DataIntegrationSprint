package com.dataintegration.core.services.storage

import com.dataintegration.core.binders.{FileStore, IntegrationConf, Properties}
import com.dataintegration.core.services.util.{ServiceFrontEnd, ServiceLayer}
import zio.{Task, ZIO, ZLayer}

object StorageManager extends ServiceFrontEnd[FileStore] {

  val live: ZLayer[IntegrationConf with ServiceLayer[FileStore], Nothing, Manager] = {
    for {
      service <- ZIO.service[ServiceLayer[FileStore]]
      integrationConf <- ZIO.service[IntegrationConf]
    } yield Manager(service, integrationConf.getFileStoreList, integrationConf.getProperties)
  }.toLayer

  private[storage] case class Manager(
                                       service: ServiceLayer[FileStore],
                                       fileStoreList: List[FileStore],
                                       properties: Properties) extends ServiceBackEnd {

    def builder(task: FileStore => Task[FileStore]): ZIO[Any, Throwable, List[FileStore]] =
      ZIO.foreachPar(fileStoreList)(task).withParallelism(properties.maxFileParallelism)

    override def onCreate: ZIO[Any, Throwable, List[FileStore]] = builder(service.onCreate)

    override def onDestroy: ZIO[Any, Throwable, List[FileStore]] = builder(service.onDestroy)

    override def getStatus: ZIO[Any, Throwable, List[FileStore]] = builder(service.getStatus)
  }

}
