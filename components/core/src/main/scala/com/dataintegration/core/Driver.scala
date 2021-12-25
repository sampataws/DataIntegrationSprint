package com.dataintegration.core

object Driver extends App with ApplicationLogger {

  def printHello(from : String): Unit = println(s"Hello $from")

  printHello(getClass.getName)
}
