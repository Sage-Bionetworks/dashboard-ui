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
   * Top by-day lists.
   */
  val TopByDay = Value("top-by-day")

  /**
   * Trending counts.
   */
  val Trending = Value("trending")

  /**
   * Time series of unique counts.
   */
  val Unique = Value("unique")

  /**
   * Counts of active users
   */
  val ActiveUser = Value("active-user")

  /**
   * File Download Report
   */
  val Report = Value("report")

  /**
   * Combine two or more charts together. No controllers.
   */
  val Summary = Value("summary")

  /**
   * An overview that includes different kinds of information. No controllers.
   */
  val Overview = Value("overview")
}
