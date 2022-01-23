package com.dataintegration.core.impl.adapter

import com.dataintegration.core.binders.IntegrationConf
import com.dataintegration.core.impl.adapter.contracts.{ComputeContract, JobContract, StorageContract}
import com.dataintegration.core.services.log.audit.DatabaseService
import com.dataintegration.core.services.log.audit.DatabaseService.AuditTableApi
import com.dataintegration.core.util.ApplicationLogger
import zio.config.ReadError
import zio.{ZIO, ZLayer}

trait IntegrationClientV2 extends zio.ZIOAppDefault with ApplicationLogger {

  val auditLayer: ZLayer[IntegrationConf, Nothing, AuditTableApi] = DatabaseService.live

  def builder[ComputeClient, StorageClient, JobClient](
                                                        configLayer: ZLayer[Any, ReadError[String], IntegrationConf],
                                                        computeContract: ComputeContract[ComputeClient],
                                                        storageContract: StorageContract[StorageClient],
                                                        jobContract: JobContract[JobClient]): ZIO[Any, Throwable, Unit] = {

    val computeDependencies = computeContract.dependencies(configLayer, auditLayer)
    val storageDependencies = storageContract.dependencies(configLayer, auditLayer)
    val jobDependencies = jobContract.dependencies(configLayer, auditLayer, computeDependencies, storageDependencies)

    val serviceResult = for {
      _ <- DatabaseService.Apis.insertInDatabase()
      _ <- jobContract.serviceManager.Apis.startService.provideLayer(jobDependencies).tapError(e =>
        DatabaseService.Apis.updateInDatabase(error = Some(e)))
      _ <- DatabaseService.Apis.updateInDatabase(error = None)
    } yield ()

    serviceResult.provideLayer(configLayer >>> auditLayer)
  }

}
