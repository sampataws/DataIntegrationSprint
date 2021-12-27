package com.dataintegration.core.binders

object Status {
  sealed trait Type

  case object Pending extends Type
  case object Running extends Type
  case object Success extends Type
  case object Failed extends Type
}
