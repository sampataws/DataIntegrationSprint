package com.dataintegration.demo.job.utils

import org.apache.spark.sql.{DataFrame, SaveMode}

object WriteFile {

  def apply(data: DataFrame, outputPath : String): Unit = {
    data.repartition(1)
      .write
      .mode(SaveMode.Overwrite)
      .option("header", "true")
      .csv(outputPath)
  }
}
