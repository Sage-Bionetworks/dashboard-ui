package controllers

import scala.collection.JavaConversions.asScalaBuffer
import org.sagebionetworks.dashboard.model.MetricType
import play.api.mvc.{Action, Controller}
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
          "")).toList
    Ok(views.html.metrics(dataMetrics))
  }

  def chartTypeAndName(metricType: MetricType) = metricType match {
    case MetricType.TIME_SERIES => ("line", "multi-line chart")
    case MetricType.TOP => ("hbar", "horizontal bar chart")
    case MetricType.UNIQUE_COUNT => ("bar", "bar chart")
  }
}
