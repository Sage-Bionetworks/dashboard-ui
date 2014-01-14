package controllers

import scala.collection.JavaConversions.asScalaBuffer

import org.joda.time.DateTime
import org.sagebionetworks.dashboard.model.{Aggregation, Statistic}
import org.sagebionetworks.dashboard.service.MetricReader

import context.AppContext
import models.{DataSeries, Metric, MetricId}
import play.api.mvc.{Action, Controller}

object Charts extends Controller with Security {

  private val metricReader = AppContext.getBean(classOf[MetricReader])

  def chart(chartType: String, metricName: String) = AuthorizedAction {
    val metric = Metric.getMetric(MetricId(chartType, metricName))
    val title = metric.fullName
    val desc = metric.description
    val start = Option(DateTime.now.minusDays(metric.startOffset).getMillis.toString)
    val end = Option(DateTime.now.minusDays(metric.endOffset).getMillis.toString)
    val interval = Option(metric.aggregation.name)
    val stat = Option(metric.statistic.name)
    Ok(views.html.chart(chartType, metricName, title, desc, start, end, interval, stat))
  }

  def data(chartType: String, metricName: String, start: Option[String], end: Option[String],
      interval: Option[String], stat: Option[String]) = AuthorizedAction {

    val metric = Metric.getMetric(MetricId(chartType, metricName))

    val from = start match {
      case Some(t) => new DateTime(t.toLong)
      case None => DateTime.now.minusDays(metric.startOffset)
    }
    val to = end match {
      case Some(t) => new DateTime(t.toLong)
      case None => DateTime.now.minusDays(metric.endOffset)
    }

    val dataPoints = chartType match {
      case "bar" => metricReader.getUniqueCount(metricName, from, to)
      case "hbar" => {
        val data = metricReader.getTop(metricName, from, 25)
        var i = 0
        for (d <- data) {
          var tmp = d
          for (converter <- metric.converters) {
            tmp = converter.convert(tmp);
          }
          data.set(i, tmp)
          i = i + 1
        }
        data
      }
      case "line" => {
        val a = interval match {
          case Some(aggr) => Aggregation.valueOf(aggr)
          case None => metric.aggregation
        }
        val s = stat match {
          case Some(a) => Statistic.valueOf(a)
          case None => Statistic.avg
        }
        metricReader.getTimeSeries(metricName, from, to, s, a)
      }
    }

    val dataSeries = new DataSeries(
      name = metric.fullName,
      description = metric.description,
      headers = List("x", ""),
      values = dataPoints.map(m => List(
          m.getX(),
          m.getY())).toList
    )

    Ok(dataSeries.toJson)
  }
}
