package com.dataintegration.core.util

import org.slf4j.Logger

object ApplicationUtils {

  /**
   * Pretty prints a seq of case classes
   *
   * @param listOfCases Seq of case class
   */
  def prettyPrintCaseClass(listOfCases: Seq[AnyRef],logger: Logger): String = {
    val getHeader = (p: AnyRef) => p.getClass.getDeclaredFields.map(x => {
      x.setAccessible(true)
      x.getName -> (if(x.get(p)== null) "null" else x.get(p).toString)
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

  def cleanForwardSlash(text : String) : String =
    text.replaceAll("//+","/")

  def updateMap[A,B](primary : Map[A,B], secondary : Map[A, B]): Map[A, B] =
    primary ++ secondary
}
