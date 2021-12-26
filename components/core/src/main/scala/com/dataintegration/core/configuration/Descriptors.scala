package com.dataintegration.core.configuration

import com.dataintegration.core.binders.{Compute, IntegrationConf}
import zio.config._
import ConfigDescriptor._

object Descriptors {

  def getComputeDescriptor: ConfigDescriptor[Compute] =
    (string("cluster_name") |@|
      string("bucket_name") |@|
      string("project") |@|
      string("region") |@|
      string("subnet_uri") |@|
      string("endpoint") |@|
      string("image_version") |@|
      string("master_machine_type_uri") |@|
      int("master_num_instance") |@|
      int("master_boot_disk_size_gb") |@|
      string("worker_machine_type_uri") |@|
      int("worker_num_instance") |@|
      int("worker_boot_disk_size_gb") |@|
      int("max_retries") |@|
      int("idle_deletion_duration_sec") |@|
      int("weightage")
      ).apply(Compute.apply, Compute.unapply)

  def getIntegrationConf: ConfigDescriptor[IntegrationConf] =
    (list("cluster_group")(getComputeDescriptor)).apply(IntegrationConf.apply, IntegrationConf.unapply)

}
