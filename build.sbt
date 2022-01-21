name := "DataIntegrationSprint"

ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.7"
ThisBuild / organization := "com.dataintegration"
ThisBuild / organizationName := "DataIntegrationSprint"

lazy val dependencies = Seq(
  "dev.zio" %% "zio" % "2.0.0-RC1",
  "dev.zio" %% "zio-config" % "1.0.10",
  "dev.zio" %% "zio-config-typesafe" % "1.0.10",
  "ch.qos.logback" % "logback-classic" % "1.2.10"
)

lazy val subProjectName = "components"

lazy val root = (project in file("."))
  .aggregate(core, gcp)

lazy val gcpLibraries = Seq(
  "com.google.cloud" % "google-cloud-dataproc" %  "2.3.1",
  "com.google.cloud" % "google-cloud-storage" % "2.2.3"
)

lazy val awsLibraries = Seq(
  "com.amazonaws" % "aws-java-sdk-emr" % "1.12.140",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.12.141"
)

lazy val azureLibraries = Seq(
  "com.microsoft.azure.hdinsight.v2018_06_01_preview" % "azure-mgmt-hdinsight" % "1.3.8",
  "com.microsoft.azure" % "azure-client-authentication" % "1.7.14",
  "com.microsoft.azure" % "azure-arm-client-runtime" % "1.7.14",
  "com.azure" % "azure-storage-blob" % "12.14.3"
)

lazy val core = (project in file(s"$subProjectName/core"))
  .settings(
    name := "core",
    libraryDependencies ++= dependencies
  )

lazy val gcp = (project in file(s"$subProjectName/gcp"))
  .settings(
    name := "gcp",
    libraryDependencies ++= gcpLibraries
  ).dependsOn(core)

lazy val aws = (project in file(s"$subProjectName/aws"))
  .settings(
    name := "aws",
    libraryDependencies ++= awsLibraries
  ).dependsOn(core)

lazy val azure = (project in file(s"$subProjectName/azure"))
  .settings(
    name := "azure",
    libraryDependencies ++= azureLibraries
  ).dependsOn(core)
