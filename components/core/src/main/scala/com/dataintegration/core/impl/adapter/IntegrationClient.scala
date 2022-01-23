package com.dataintegration.core.impl.adapter

import com.dataintegration.core.binders.{IntegrationConf, JobConfig}
import com.dataintegration.core.impl.adapter.contracts.{ComputeContract, JobContract, StorageContract}
import com.dataintegration.core.services.log.audit.DatabaseServiceV2
import com.dataintegration.core.services.log.audit.DatabaseServiceV2.AuditTableApi
import com.dataintegration.core.util.ApplicationLogger
import zio.config.ReadError
import zio.{ZIO, ZLayer}

trait IntegrationClient extends zio.ZIOAppDefault with ApplicationLogger {

  val auditLayer: ZLayer[IntegrationConf, Nothing, AuditTableApi] = DatabaseServiceV2.live

  def builder[ComputeClient, StorageClient, JobClient](
                                                        configLayer: ZLayer[Any, ReadError[String], IntegrationConf],
                                                        computeContract: ComputeContract[ComputeClient],
                                                        storageContract: StorageContract[StorageClient],
                                                        jobContract: JobContract[JobClient]): ZIO[Any, Throwable, List[JobConfig]] = {

    val computeDependencies = computeContract.dependencies(configLayer, auditLayer)
    val storageDependencies = storageContract.dependencies(configLayer, auditLayer)
    val jobDependencies = jobContract.dependencies(configLayer, auditLayer, computeDependencies, storageDependencies)
    jobContract.serviceManager.Apis.startService.provideLayer(jobDependencies)
  }

}
