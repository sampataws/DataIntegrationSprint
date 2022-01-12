package com.dataintegration.core.services.util

case class ServiceResult[T <: ServiceConfig, S](config: T, result: S)
