package com.dataintegration.demo.job.utils

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col, concat_ws, md5}


object CompareData {

  def data(actualOutputDataFrame: DataFrame, expectedOutputDataFrame: DataFrame): Boolean = {
    val actual = generateKey(actualOutputDataFrame)
    val expected = generateKey(expectedOutputDataFrame)
    val antiJoin = actual.join(expected, Seq("join_key"), "left_anti")
    val semiJoin = actual.join(expected, Seq("join_key"), "left_semi")
    (semiJoin.count() == expectedOutputDataFrame.count()) && antiJoin.count() == 0
  }

  private def generateKey(dataFrame: DataFrame): DataFrame = {
    val columnList = dataFrame.columns.map(col)
    val concatKey = concat_ws("_", columnList: _*)
    dataFrame.select(md5(concatKey).alias("join_key"))
  }
}
