package com.dataintegration.core.configuration

import com.dataintegration.core.binders.{Cluster, IntegrationConf, Status}
import zio.config._
import ConfigDescriptor._

object Descriptors {

  def getComputeDescriptor: ConfigDescriptor[Cluster] =
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
      int("weightage") |@|
      addStatusColumn()
      ).apply(Cluster.apply, Cluster.unapply)

  def getIntegrationConf: ConfigDescriptor[IntegrationConf] =
    (list("cluster_group")(getComputeDescriptor)).apply(IntegrationConf.apply, IntegrationConf.unapply)

  def addStatusColumn() : ConfigDescriptor[Status.Type] =
    string("status").optional.transform[Status.Type](_ => Status.Pending, s => Option(s.toString))

}
