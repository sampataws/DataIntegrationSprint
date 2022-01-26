package com.dataintegration.demo.job.rules

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.col

object CountryFilter {

  def apply(countryList : Seq[String])(data : DataFrame): DataFrame = {
    data.filter(col("location").isin(countryList :_*))
  }

}
