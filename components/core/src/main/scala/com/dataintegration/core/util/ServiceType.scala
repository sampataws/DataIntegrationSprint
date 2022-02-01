package com.dataintegration.core.util

object ServiceType {

  sealed trait Type

  case object Compute extends Type

  case object Storage extends Type

  case object JobSubmit extends Type

}
