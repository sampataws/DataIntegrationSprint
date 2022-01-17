package com.dataintegration.core.impl.adapter

import com.dataintegration.core.binders.{IntegrationConf, JobConfig}
import com.dataintegration.core.util.ApplicationLogger
import zio.config.ReadError
import zio.{ZIO, ZLayer}

trait IntegrationClient extends zio.ZIOAppDefault with ApplicationLogger {

  def builder[ComputeClient, StorageClient, JobClient](
                                                        configLayer: ZLayer[Any, ReadError[String], IntegrationConf],
                                                        computeContract: ComputeContract[ComputeClient],
                                                        storageContract: StorageContract[StorageClient],
                                                        jobContract: JobContract[JobClient]): ZIO[Any, Throwable, List[JobConfig]] = {

    val computeDependencies = computeContract.dependencies(configLayer)
    val storageDependencies = storageContract.dependencies(configLayer)
    val jobDependencies = jobContract.dependencies(configLayer, computeDependencies, storageDependencies)
    jobContract.serviceManager.Apis.startService.provideLayer(jobDependencies)
  }

}
