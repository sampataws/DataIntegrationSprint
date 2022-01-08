package com.dataintegration.core.configuration

import java.time.format.DateTimeFormatter
import java.util.UUID

import zio.config._
import ConfigDescriptor._
import com.dataintegration.core.binders._
import com.dataintegration.core.util.Status

object Descriptors {

  def getComputeDescriptor: ConfigDescriptor[Cluster] =
    (applyFunctionalTransformation(string("cluster_name")) |@|
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
      int("idle_deletion_duration_sec") |@|
      int("weightage") |@|
      addColumn[Status.Type]("status", Status.Pending) |@|
      addColumn("error_message", Seq.empty[String])
      ).apply(Cluster.apply, Cluster.unapply)

  def getPropertiesDescriptor: ConfigDescriptor[Properties] =
    (string("job_name") |@|
      string("source_system") |@|
      string("main_class") |@|
      string("working_directory") |@|
      list("args")(string) |@|
      map("spark_conf")(string) |@|
      nested("jars")(list("jar_dependencies")(getFileStoreDescriptor)) |@|
      int("max_cluster_parallelism") |@|
      int("max_cluster_retries") |@|
      boolean("clean_up_directory")
      ).apply(Properties.apply, Properties.unapply)

  def getFeatureDescriptor: ConfigDescriptor[Feature] =
    (string("name") |@|
      applyFunctionalTransformation(string("base_path")) |@|
      string("main_class").optional |@|
      nested("storage")(list("file_dependencies")(getFileStoreDescriptor)) |@|
      list("args")(string).optional |@|
      map("spark_conf")(string).optional |@|
      boolean("runnable") |@|
      addColumn[Status.Type]("status", Status.Pending) |@|
      addColumn("error_message", Seq.empty[String])
      ).apply(Feature.apply, Feature.unapply)

  def getFileStoreDescriptor: ConfigDescriptor[FileStore] =
    (string("source_bucket") |@|
      string("source_path") |@|
      string("target_bucket").optional |@|
      string("target_path").optional |@|
      addColumn[Status.Type]("status", Status.Pending) |@|
      addColumn("error_message", Seq.empty[String])
      ).apply(FileStore.apply, FileStore.unapply)

  def getIntegrationConf: ConfigDescriptor[IntegrationConf] =
    (list("cluster_group")(getComputeDescriptor) |@|
      list("features")(getFeatureDescriptor) |@|
      nested("project_properties")(getPropertiesDescriptor)
      ).apply(IntegrationConf.apply, IntegrationConf.unapply)

  private def addColumn[T](name: String, transformation: => T): ConfigDescriptor[T] =
    string(name).optional.transform[T](_ => transformation , s => Option(s.toString))

  private def applyFunctionalTransformation(text: ConfigDescriptor[String]): ConfigDescriptor[String] = {
    val getUUID = () => UUID.randomUUID().toString

    val getDataTime = () => DateTimeFormatter.ofPattern("YYYYMMdd").format(java.time.LocalDateTime.now())

    val transformations = Seq("@{uuid}" -> getUUID, "@{today}" -> getDataTime)

    val transform = (text: String) => transformations.foldLeft(text) { (text, transformations) =>
      text.replace(transformations._1, transformations._2())
    }

    text.transform[String](transform, _.toString)
  }

}
