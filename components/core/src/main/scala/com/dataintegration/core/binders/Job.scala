package com.dataintegration.core.binders

import com.dataintegration.core.services.util.Status

case class Job(
                name: String,
                programArguments: Seq[String],
                className: String,
                sparkConf : Map[String, String],
                libraryList: Seq[String],
                status: Status.Type = Status.Pending,
                errorMessage: Seq[String] = Seq.empty
              )
