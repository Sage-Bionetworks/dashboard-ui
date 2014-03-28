package models

/**
 * The type of the metrics determines how the metric will be rendered.
 */
object MetricType extends Enumeration {

  /**
   * Categorical data.
   */
  val Category = Value("category")

  /**
   * Time series of latencies.
   */
  val Latency = Value("latency")

  /**
   * Top lists.
   */
  val Top = Value("top")

  /**
   * Time series of unique counts.
   */
  val Unique = Value("unique")

  /**
   * Counts of active users
   */
  val ActiveUser = Value("active-user")
}
