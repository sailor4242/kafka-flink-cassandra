package me.sandbox.sql.streaming.source.rate

import org.apache.spark.sql.functions.{col, lit, pmod, rand}
import org.apache.spark.sql.{DataFrame, SparkSession}


class UserActionsRateSource(val spark: SparkSession,
                            val rowsPerSecond: String = "5",
                            val numPartitions: String = "1") extends RateSource {
  def loadUserActions(): DataFrame = {
    readStream()
      .where((rand() * 100).cast("integer") < 30) // 30 out of every 100 user actions
      .select(pmod(col("value"), lit(9)).as("userId"), col("timestamp").as("actionTime"))
  }


}