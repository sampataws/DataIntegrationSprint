package com.dataintegration.demo.job.rules

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col,sum}

object DayCount {

  def apply(data : DataFrame): DataFrame = {
    data.groupBy("date").agg(sum("num_sequences").alias("total_day_count"))
  }

}
