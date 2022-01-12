package com.dataintegration.core.services.utilv2

import com.dataintegration.core.services.util.ServiceConfig

case class ServiceResult[T <: ServiceConfig, S](config: T, result: Option[S])
