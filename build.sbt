name := "DataIntegrationSprint"

ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.12.15"
ThisBuild / organization := "com.dataintegration"
ThisBuild / organizationName := "DataIntegrationSprint"

lazy val dependencies = Seq(
  "dev.zio" %% "zio" % "2.0.0-RC1",
  "dev.zio" %% "zio-config" % "1.0.10",
  "dev.zio" %% "zio-config-typesafe" % "1.0.10",
  "ch.qos.logback" % "logback-classic" % "1.2.10"
)

lazy val subProjectName = "components"
lazy val subProjectNameExamples = "examples"
lazy val scalaVersionsToCompile = Seq("2.13.7", "2.12.15")

lazy val root = (project in file("."))
  .aggregate(core, gcp)

lazy val gcpLibraries = Seq(
  "com.google.cloud" % "google-cloud-dataproc" % "2.3.1",
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

lazy val databaseLibraries = Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "4.0.0",
  "mysql" % "mysql-connector-java" % "8.0.28"
)

lazy val core = (project in file(s"$subProjectName/core"))
  .settings(
    name := "core",
    crossScalaVersions := scalaVersionsToCompile,
    libraryDependencies ++= dependencies
  )

lazy val gcp = (project in file(s"$subProjectName/gcp"))
  .settings(
    name := "gcp",
    libraryDependencies ++= gcpLibraries
  ).dependsOn(core)

lazy val gcpManaged = (project in file(s"$subProjectName/gcp-managed"))
  .settings(
    name := "gcp-managed",
    crossScalaVersions := scalaVersionsToCompile,
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

lazy val database = (project in file(s"$subProjectName/database"))
  .settings(
    name := "database",
    libraryDependencies ++= databaseLibraries
  ).dependsOn(core)

lazy val sparkLibraries = Seq(
  "org.apache.spark" %% "spark-core" % "3.1.2" % "provided",
  "org.apache.spark" %% "spark-sql" % "3.1.2" % "provided"
)
lazy val gcpDemo = (project in file(s"$subProjectNameExamples/gcp-demo"))
  .settings(
    name := "gcp-demo",
    crossScalaVersions := scalaVersionsToCompile,
    libraryDependencies ++= sparkLibraries,
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  ).dependsOn(core, gcpManaged, database)


