package com.dataintegration.core.services.util

case class ServiceResult[C <: ServiceConfig, R](config: C, result: R)
