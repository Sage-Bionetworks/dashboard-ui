package models

case class DataMetric(
  id: String,
  name: String,
  description: String,
  chartType: String,
  chartName: String,
  queryString: String
)
