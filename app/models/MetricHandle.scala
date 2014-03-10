package models

/**
 * Uniquely identifies a metric rendered by the UI.
 *
 * @param metricType  The type of the metric
 * @param metricName  The name of the stored metric
 */
case class MetricHandle (metricType: MetricType.Value, metricName: String)
