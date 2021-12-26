package com.dataintegration.core.util

object ApplicationUtils {

  /**
   * Pretty prints a seq of case classes
   *
   * @param listOfCases Seq of case class
   */
  def prettyPrintCaseClass(listOfCases: Seq[AnyRef]): Unit = {
    val getHeader = (p: AnyRef) => p.getClass.getDeclaredFields.map(x => {
      x.setAccessible(true)
      x.getName -> x.get(p).toString
    })

    val createString = (text: String, maxLength: Int) => {
      val ttl = maxLength - text.length
      s"| $text " + " " * ttl
    }

    val printLineBreak = (length: Int) =>
      println("\n" + "-" * length)

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
      print(createString(x._1, headerWithMaxLength(x._1)))
    }

    print("\n" + "-" * maxTextBreaks)

    header.foreach { x =>
      println()
      x.foreach(y => print(createString(y._2, headerWithMaxLength(y._1))))

    }
    printLineBreak(maxTextBreaks)
    println()
  }

}
