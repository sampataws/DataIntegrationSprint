package backup.everythingConf

import java.io.File
import java.util.UUID

import com.dataintegration.core.util.{ApplicationLogger, ApplicationUtils, Status}
import zio.config._
import ConfigDescriptor._
import com.dataintegration.core.services.configuration.Descriptors.addColumn
import zio.config.typesafe.TypesafeConfigSource

object Configuration extends App with ApplicationLogger {
  private val str =
    """
      |{
      |"name" : "abc"
      |"jars" : {
      | "dep" : ["a1","a2"]
      |}
      |}
      |""".stripMargin

  private case class TEST(serviceId: String,status: Status.Type, name: String, jars: List[String])

  private case class TESTFILE(bucket: String, path: String)

  private def getDesc = (
    addColumn("service_id", Seq("abc", "def").mkString(",")) |@|
      addColumn[Status.Type]("status", Status.Pending) |@|
      string("name") |@|
      nested("jars")(list("dep")(string))
    ) (TEST.apply, TEST.unapply)

  private def getFiles = (
    string("source_bucket") |@| string("source_path")
    ) (TESTFILE.apply, TESTFILE.unapply)

  private lazy val conf =
    TypesafeConfigSource.fromHoconString(str)
      .flatMap(x => read(getDesc from x))

  private lazy val readableConf = conf match {
    case Left(ex) => throw new Exception(ex)
    case Right(value) => value
  }

  private def addColumn[T](name: String, transformation: => T): ConfigDescriptor[T] =
    string(name).optional.transform[T](_ => transformation, s => Option(s.toString))

  private def addServiceIdColumn(): ConfigDescriptor[String] =
    string("service_id").optional.transform[String]((abc: Option[String]) => UUID.randomUUID().toString, s => Option(s.toString))

  private def addStatusColumn(): ConfigDescriptor[Status.Type] =
    string("status").optional.transform[Status.Type]((abc: Option[String]) => Status.Pending, s => Option(s.toString))

  private def addErrorColumn(): ConfigDescriptor[Seq[String]] =
    string("error_message").optional.transform[Seq[String]]((abc: Option[String]) => Seq.empty[String], s => Option(s.toString))

  private def addColumnV2[T](name: String, transformation: => T): ConfigDescriptor[T] =
    string(name).optional.transform[T](_ => transformation, s => Option(s.toString))


  ApplicationUtils.prettyPrintCaseClass(Seq(readableConf), logger)

}
