package models

/**
 * Uniquely identifies a metric rendered by the UI. This is the bridge
 * between the metrics stored and the metrics rendered.
 *
 * @param chartType One of the "bar", "hbar", "line"
 * @param metricName The name of the stored metric
 */
case class MetricId (chartType: String, metricName: String)
