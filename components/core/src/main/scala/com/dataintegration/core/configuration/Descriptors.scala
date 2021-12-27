package com.dataintegration.core.configuration

import java.time.format.DateTimeFormatter
import java.util.UUID

import zio.config._
import ConfigDescriptor._
import com.dataintegration.core.binders.{Cluster, Feature, FileStore, IntegrationConf, Properties}
import com.dataintegration.core.services.util.Status

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

  def getPropertiesDescriptor: ConfigDescriptor[Properties] =
    (string("job_name") |@|
      string("source_system") |@|
      int("max_cluster_retries") |@|
      int("max_cluster_parallelism") |@|
      int("max_file_retries") |@|
      int("max_file_transfer_parallelism") |@|
      int("max_job_parallelism") |@|
      list("args")(string) |@|
      string("working_directory") |@|
      boolean("clean_up_directory") |@|
      string("main_class") |@|
      map("spark_conf")(string)
      ).apply(Properties.apply, Properties.unapply)

  def getFeatureDescriptor: ConfigDescriptor[Feature] =
    (string("name") |@|
      string("base_path") |@|
      string("main_class").optional |@|
      boolean("runnable") |@|
      list("args")(string).optional |@|
      map("spark_conf")(string).optional |@|
      addStatusColumn()
      ).apply(Feature.apply, Feature.unapply)

  def getFileStoreDescriptor: ConfigDescriptor[FileStore] =
    (string("source_bucket") |@|
      string("source_path") |@|
      string("target_bucket").optional |@|
      string("target_path").optional |@|
      addStatusColumn()
    ).apply(FileStore.apply, FileStore.unapply)

  def getIntegrationConf: ConfigDescriptor[IntegrationConf] =
    (list("cluster_group")(getComputeDescriptor) |@|
      list("features")(getFeatureDescriptor) |@|
      map("files_transfer")(list(getFileStoreDescriptor)) |@|
      nested("project_properties")(getPropertiesDescriptor)
      ).apply(IntegrationConf.apply, IntegrationConf.unapply)

  private def addStatusColumn(): ConfigDescriptor[Status.Type] =
    string("status").optional.transform[Status.Type](_ => Status.Pending, s => Option(s.toString))

  private def addErrorColumn(): ConfigDescriptor[Seq[String]] =
    string("error_message").optional.transform[Seq[String]](_ => Seq.empty[String], s => Option(s.toString))

  private def applyFunctionalTransformation(text: ConfigDescriptor[String]): ConfigDescriptor[String] = {
    def getUUID: String = UUID.randomUUID().toString

    def getDataTime(pattern: String = "YYYYMMdd"): String = DateTimeFormatter.ofPattern(pattern).format(java.time.LocalDateTime.now())

    val transformations = Seq("@{uuid}" -> getUUID, "@{today}" -> getDataTime())

    val transform = (text: String) => transformations.foldLeft(text) { (text, transformations) =>
      text.replace(transformations._1, transformations._2)
    }

    text.transform[String](transform, _.toString)
  }

}
