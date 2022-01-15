package com.dataintegration.core

import com.dataintegration.core.util.ApplicationLogger

/**
 * Services
 *  - Config
 *  - Compute -> create and destroy cluster
 *  - Storage -> upload/move file and delete files
 *  - Spark Submit -> run job
 *
 *  - Manager
 *    - dependency
 * 1 - Config
 * 2 - Audit
 * 3 - Compute and storage
 * 4 - Run job
 * 5 - Clean Up ???
 *
 *  - Service
 *    - Initializer
 *    - CleanUp
 *
 *  - Service Conf
 *    - Success and failure Case
 *    - Log in console and audit
 *
 */
object Driver extends App with ApplicationLogger {

  def printHello(from: String): Unit = println(s"Hello $from")

  printHello(getClass.getName)
}
