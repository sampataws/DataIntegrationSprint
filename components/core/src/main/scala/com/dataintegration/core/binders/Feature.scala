package com.dataintegration.core.binders

import com.dataintegration.core.services.util.Status

case class Feature(
                  name : String,
                  basePath : String,
                  mainClass : Option[String],
                  executableFlag : Boolean,
                  arguments : Option[List[String]],
                  sparkConf : Option[Map[String, String]],
                  status : Status.Type,
                  errorMessage : Seq[String])
