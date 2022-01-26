package com.dataintegration.demo.job.rules

import org.apache.spark.sql.DataFrame

object VariantCount {

  def apply(data : DataFrame): DataFrame = {
    data.groupBy("variant").sum("num_sequences")
  }

}
