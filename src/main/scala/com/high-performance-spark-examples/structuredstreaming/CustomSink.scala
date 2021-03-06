/**
 * A simple custom sink to allow us to train our models on micro-batches of data.
 */
package com.highperformancespark.examples.structuredstreaming

import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql._
import org.apache.spark.sql.sources.{DataSourceRegister, StreamSinkProvider}
import org.apache.spark.sql.execution.streaming.Sink


/**
 * Creates a custom sink similar to the old foreachRDD. Provided function is called for each
 * time slice with the dataset representing the time slice.
 * Provided func must consume the dataset (e.g. call `foreach` or `collect`).
 * As per SPARK-16020 arbitrary transformations are not supported, but converting to an RDD
 * will allow for more transformations beyond `foreach` and `collect` while preserving the
 * incremental planning.
 */
abstract class ForeachDatasetSinkProvider extends StreamSinkProvider {
  def func(df: DataFrame): Unit

  def createSink(
      sqlContext: SQLContext,
      parameters: Map[String, String],
      partitionColumns: Seq[String],
      outputMode: OutputMode): ForeachDatasetSink = {
    new ForeachDatasetSink(func)
  }
}

/**
 * Custom sink similar to the old foreachRDD.
 * To use with the stream writer - do not construct directly, instead subclass
 * [[ForeachDatasetSinkProvider]] and provide to Spark's DataStreamWriter format.
 *  This can also be used directly as in StreamingNaiveBayes.scala
 */
case class ForeachDatasetSink(func: DataFrame => Unit)
    extends Sink {

  val estimator = new StreamingNaiveBayes()

  override def addBatch(batchId: Long, data: DataFrame): Unit = {
    func(data)
  }
}
