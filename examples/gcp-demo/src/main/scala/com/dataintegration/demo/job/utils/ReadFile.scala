package com.dataintegration.demo.job.utils

import org.apache.spark.sql.functions.{col, to_date}
import org.apache.spark.sql.{DataFrame, SparkSession}

object ReadFile {

  def apply(path: String): DataFrame = {
    val spark = SparkSession.builder().getOrCreate()

    spark
      .read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(path)
      .withColumn("date", to_date(col("date"), "yyyy-mm-dd"))

  }

}
