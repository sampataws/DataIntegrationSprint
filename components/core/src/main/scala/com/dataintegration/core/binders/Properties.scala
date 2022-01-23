package com.dataintegration.core.binders

import java.util.UUID

case class Properties(
                       jobId : String = UUID.randomUUID().toString,
                       jobName: String,
                       systemName: String,
                       parentMainClass: String,
                       parentWorkingDir: String,
                       jobArguments: List[String],
                       jobSparkConf: Map[String, String],
                       jarDependencies: List[FileStoreConfig],
                       maxParallelism: Int,
                       maxRetries: Int,
                       cleanUpFlag: Boolean
                     )