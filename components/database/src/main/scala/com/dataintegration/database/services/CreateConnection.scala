package com.dataintegration.database.services

import scalikejdbc.ConnectionPool
import zio.{Task, URIO, ZLayer, ZManaged}

object CreateConnection {
  def createClient: Task[Unit] = Task {
    val sqlCredentials = SqlCredentials()
    Class.forName(sqlCredentials.driver)
    ConnectionPool.singleton(sqlCredentials.url, sqlCredentials.user, sqlCredentials.password)
    println("connection created")
  }

  def destroyClient: URIO[Any, Unit] = Task {
    ConnectionPool.closeAll()
    println("connection destroyed")
  }.orDie

  val live: ZLayer[Any, Throwable, Unit] =
    ZManaged.acquireReleaseWith(acquire = createClient)(release = _ => destroyClient).toLayer
}
