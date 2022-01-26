package com.dataintegration.database.services

import com.dataintegration.core.util.{ApplicationLogger, SqlCredentials}
import scalikejdbc.ConnectionPool
import zio.{Task, ZLayer, ZManaged}

object CreateConnection extends ApplicationLogger {
  def createClient: Unit = {
    val sqlCredentials = SqlCredentials()
    Class.forName(sqlCredentials.driver)
    ConnectionPool.singleton(sqlCredentials.url, sqlCredentials.user, sqlCredentials.password)
    logger.info("Database connection created")
  }

  def destroyClient: Unit = {
    ConnectionPool.closeAll()
    logger.info("Database connection destroyed")
  }

  val live: ZLayer[Any, Throwable, Unit] =
    ZManaged.acquireReleaseWith(acquire = Task(createClient))(release = _ => Task(destroyClient).orDie).toLayer
}
