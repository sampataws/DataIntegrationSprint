package com.dataintegration.core.binders

case class Properties(
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