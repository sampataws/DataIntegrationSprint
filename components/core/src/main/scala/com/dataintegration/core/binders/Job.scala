package com.dataintegration.core.binders

import com.dataintegration.core.services.util.Status

case class Job(
                name: String,
                programArguments: List[String],
                className: String,
                libraryList: Seq[String],
                status: Status.Type,
                errorMessage: Seq[String]
              )
