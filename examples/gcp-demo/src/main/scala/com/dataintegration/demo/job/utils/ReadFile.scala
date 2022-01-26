package com.dataintegration.demo.job.utils

import org.apache.spark.sql.functions.{col, to_date}
import org.apache.spark.sql.{DataFrame, SparkSession}

object ReadFile {

  def apply(path: String): DataFrame = {
    val path = "C:\\Users\\amber\\IdeaProjects\\DataIntegrationSprint\\examples\\gcp-demo\\src\\main\\resources\\rulesvalidation\\input\\covid_variants.csv"

    val spark = SparkSession.builder().master("local[2]").getOrCreate()

    spark
      .read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(path)
      .withColumn("date", to_date(col("date"), "yyyy-mm-dd"))

  }

}
