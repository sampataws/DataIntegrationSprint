package com.dataintegration.gcp.services.compute.application

import com.dataintegration.core.binders.{ComputeConfig, Properties}
import com.google.cloud.dataproc.v1.ClusterControllerClient

case class CreateCluster(data: ComputeConfig, properties: Properties, client: ClusterControllerClient) {

}
