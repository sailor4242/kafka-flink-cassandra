package me.sandbox.sql.streaming.source

import org.apache.spark.sql.DataFrame

trait StreamingSource {
  def readStream: DataFrame
}
