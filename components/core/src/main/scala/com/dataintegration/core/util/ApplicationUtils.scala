package com.dataintegration.core.util

import org.slf4j.Logger

object ApplicationUtils {

  /**
   * Pretty prints a seq of case classes
   *
   * @param listOfCases Seq of case class
   */
  def prettyPrintCaseClass(listOfCases: Seq[AnyRef], logger: Logger): String = {
    if (listOfCases.isEmpty) return "List is empty"
    val getHeader = (p: AnyRef) => p.getClass.getDeclaredFields.map(x => {
      x.setAccessible(true)
      x.getName -> (if (x.get(p) == null) "null" else x.get(p).toString)
    })

    var result = "Service Output"

    val createString = (text: String, maxLength: Int) => {
      val ttl = maxLength - text.length
      s"| $text " + " " * ttl
    }

    val printLineBreak = (length: Int) =>
      result += ("\n" + "-" * length) + "\n"

    val header = listOfCases.map(getHeader)

    val headerWithMaxLength = header.flatten.foldLeft(Map[String, Int]().empty) { (map, data) =>
      val currentLength = if (data._2.length > data._1.length) data._2.length else data._1.length
      val up = map.getOrElse(data._1, currentLength)
      val maxLength = if (up > currentLength) up else currentLength
      map ++ Map(data._1 -> maxLength)
    }
    val maxTextBreaks = (headerWithMaxLength.values.sum + headerWithMaxLength.keys.size + 2 * headerWithMaxLength.keys.size)

    printLineBreak(maxTextBreaks)

    header.head.foreach { x =>
      result += (createString(x._1, headerWithMaxLength(x._1)))
    }

    result += ("\n" + "-" * maxTextBreaks)

    header.foreach { x =>
      result += "\n"
      x.foreach(y => result += (createString(y._2, headerWithMaxLength(y._1))))

    }
    printLineBreak(maxTextBreaks)
    result += "\n"

    logger.info(result)
    result
  }

  def cleanForwardSlash(text: String): String =
    text.replaceAll("//+", "/")

  def updateMap[A, B](primary: Map[A, B], secondary: Map[A, B]): Map[A, B] =
    primary ++ secondary

  def mapToJson[I, O](data: Map[I, O]): String =
    if (data.isEmpty) "{}"
    else {
      val fixStringType = (data: String) => s""""$data""""
      "{" + data.map { self => fixStringType(self._1.toString) + ":" + fixStringType(self._2.toString) }.mkString(", ") + "}"
    }

  def jsonToMap(data: String): Map[String, String] = try {
    val filteredData = data.replaceAll("\\{", "").replaceAll("}", "").trim
    if (filteredData == "") Map.empty[String, String]
    else filteredData.split(",").map { value =>
      val splitData = value.split(":")
      splitData.head.trim -> splitData.last.trim
    }.toMap
  } catch {
    case e: Throwable => Map.empty[String, String]
  }

  @scala.annotation.tailrec
  def equallyDistributeList[C, J](
                                   primaryList: List[J],
                                   distributionList: List[C],
                                   accumulator: Map[J, C] = Map.empty[J, C]): Map[J, C] = {

    if (distributionList.isEmpty) throw new RuntimeException("Not enough clusters running to create spark job!")

    if (primaryList.isEmpty) accumulator
    else
      equallyDistributeList(
        primaryList = primaryList.tail,
        distributionList = distributionList.tail :+ distributionList.head,
        accumulator = accumulator ++ Map(primaryList.head -> distributionList.head))
  }
}
