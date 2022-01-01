package com.dataintegration.core.binders

import com.dataintegration.core.services.util.{ServiceConfig, Status}

case class FileStore(
                      sourceBucket: String,
                      sourcePath: String,
                      targetBucket: Option[String],
                      targetPath: Option[String],
                      status: Status.Type,
                      errorMessage: Seq[String]
                    ) extends ServiceConfig
