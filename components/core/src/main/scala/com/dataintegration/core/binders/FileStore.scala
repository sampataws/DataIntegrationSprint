package com.dataintegration.core.binders

import com.dataintegration.core.services.util.Status

case class FileStore(
                      sourceBucket: String,
                      sourcePath: String,
                      targetBucket: Option[String],
                      targetPath: Option[String],
                      status: Status.Type,
                      errorMessage: Seq[String]
                    )
