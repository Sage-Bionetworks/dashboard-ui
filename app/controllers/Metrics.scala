package controllers

import scala.collection.JavaConversions._
import play.api.mvc.{Action, Controller}
import org.sagebionetworks.dashboard.model.MetricType
import org.sagebionetworks.dashboard.service.MetricToRead
import models.DataMetric

object Metrics extends Controller {

  def all = Action {
    val metrics = ApplicationContext.metrics
    val dataMetrics = metrics.map(m =>
        DataMetric(
          m.getId,
          m.getName,
          m.getDescription,
          chartTypeAndName(m.getType)._1,
          chartTypeAndName(m.getType)._2,
          queryString(m))).toList
    Ok(views.html.metrics(dataMetrics))
  }

  def chartTypeAndName(metricType: MetricType) = metricType match {
    case MetricType.TIME_SERIES => ("line", "multi-line chart")
    case MetricType.TOP => ("hbar", "horizontal bar chart")
    case MetricType.UNIQUE_COUNT => ("bar", "bar chart")
  }

  def queryString(metric: MetricToRead) = metric.getType() match {
    case MetricType.TIME_SERIES => {
      "start=" +
      metric.getDefaultStart().minusDays(14).toString("yyyyMMddHH") +
      "&end=" +
      metric.getDefaultEnd().minusDays(14).toString("yyyyMMddHH") +
      "&interval=" +
      metric.getDefaultAggregation +
      "&stat=" +
      metric.getDefaultStatistic
    }
    case MetricType.TOP => {
      "start=" + metric.getDefaultStart().minusDays(14).toString("yyyyMMdd")
    }
    case MetricType.UNIQUE_COUNT => {
      "start=" +
      metric.getDefaultStart().minusDays(14).toString("yyyyMMdd") +
      "&end=" +
      metric.getDefaultEnd().minusDays(14).toString("yyyyMMdd")
    }
  }
}
