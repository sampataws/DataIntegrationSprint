package com.dataintegration.core.impl.adapter

import com.dataintegration.core.binders.{IntegrationConf, JobConfig}
import com.dataintegration.core.impl.adapter.contracts.{ComputeContract, JobContract, StorageContract}
import com.dataintegration.core.services.log.audit.DatabaseService
import com.dataintegration.core.services.log.audit.DatabaseService.AuditTableApi
import com.dataintegration.core.util.ApplicationLogger
import zio.config.ReadError
import zio.{IsNotIntersection, Tag, ZIO, ZLayer}

trait IntegrationClient extends zio.ZIOAppDefault with ApplicationLogger {

  val auditLayer: ZLayer[IntegrationConf, Nothing, AuditTableApi] = DatabaseService.live

  def builder[ComputeClient: Tag : IsNotIntersection, StorageClient: Tag : IsNotIntersection, JobClient: Tag : IsNotIntersection]
  (
    configLayer: ZLayer[Any, ReadError[String], IntegrationConf],
    computeContract: ComputeContract[ComputeClient],
    storageContract: StorageContract[StorageClient],
    jobContract: JobContract[JobClient]): ZIO[Any, Throwable, List[JobConfig]] = {

    val computeDependencies = computeContract.partialDependencies
    val storageDependencies = storageContract.partialDependencies
    val jobDependencies = jobContract.partialDependencies

    val appDependencies = ((computeDependencies ++ storageDependencies) >>> jobDependencies)

    val serviceResult = for {
      _ <- DatabaseService.Apis.insertInDatabase()
      response <- jobContract.serviceManager.Apis.startService.provideLayer(appDependencies).tapError(exception =>
        DatabaseService.Apis.updateInDatabase(error = Some(exception)))
      _ <- DatabaseService.Apis.updateInDatabase(error = None)
    } yield response

    serviceResult.provideLayer(configLayer >+> auditLayer)

    /**
     * other solutions
     *
     * 1st
     * .provideLayer(configLayer >>> appDependencies)
     * serviceResult.provideLayer(configLayer >>> auditLayer)
     *
     * 2nd
     * .provideLayer(appDependencies)
     * serviceResult.provideLayer(configLayer >>> auditLayer ++ configLayer)
     *
     * 3rd
     * .provideLayer(appDependencies)
     * serviceResult.provideLayer(configLayer >+> auditLayer)
     */

  }

}
