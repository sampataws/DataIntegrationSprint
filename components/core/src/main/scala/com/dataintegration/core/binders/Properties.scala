package com.dataintegration.core.binders

case class Properties(
                       jobName: String,
                       sourceSystem: String,
                       maxClusterRetries: Int,
                       maxClusterParallelism: Int,
                       maxFileRetires: Int,
                       maxFileParallelism: Int,
                       maxJobParallelism: Int,
                       arguments: List[String],
                       workingDir: String,
                       cleanUpFlag: Boolean,
                       mainClass: String,
                       sparkConf: Map[String, String]
                     )