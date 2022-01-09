package com.dataintegration.core.binders

import com.dataintegration.core.util.Status

case class Feature(
                    name: String,
                    basePath: String,
                    mainClass: Option[String],
                    fileDependencies: List[FileStore],
                    arguments: Option[List[String]],
                    sparkConf: Option[Map[String, String]],
                    executableFlag: Boolean,
                    status: Status.Type,
                    errorMessage: Seq[String]
                  )

