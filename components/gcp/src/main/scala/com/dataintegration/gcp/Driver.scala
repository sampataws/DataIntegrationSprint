package com.dataintegration.gcp
import com.dataintegration.core.{Driver => CoreDriver}

object Driver extends App {
  CoreDriver.printHello(getClass.getName)
}
