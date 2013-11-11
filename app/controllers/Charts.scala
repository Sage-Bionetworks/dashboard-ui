package controllers

import scala.collection.JavaConversions._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.mvc.{Action, Controller}
import org.sagebionetworks.dashboard.model.MetricType
import org.sagebionetworks.dashboard.model.redis.Aggregation
import org.sagebionetworks.dashboard.model.redis.Statistic
import org.sagebionetworks.dashboard.service.MetricQueryService
import models.DataSeries

object Charts extends Controller {

  private val metricQuerySrv = ApplicationContext.getBean(classOf[MetricQueryService])

  def bar(metricId: String, start: Option[String], end: Option[String]) = Action {

    val metric = ApplicationContext.getMetric(metricId, MetricType.UNIQUE_COUNT)

    val format = DateTimeFormat.forPattern("yyyyMMdd")
    val from = start match {
      case Some(t) => format.parseDateTime(t)
      case None => metric.getDefaultStart
    }
    val to = end match {
      case Some(t) => format.parseDateTime(t)
      case None => metric.getDefaultEnd()
    }

    val dataPoints = metricQuerySrv.getUniqueCount(metricId, from, to)
    val dataSeries = new DataSeries(
      name = metric.getName,
      description = metric.getDescription,
      headers = List("Timestamp", "All"),
      values = dataPoints.map(m => List(
          (new DateTime(m.getTimestampInMs())).toString("MM/dd"),
          m.getY())).toList
    )

    Ok(views.html.barchart(dataSeries))
  }

  def hbar(metricId: String, start: Option[String]) = Action {
    val metric = ApplicationContext.getMetric(metricId, MetricType.TOP)
    val format = DateTimeFormat.forPattern("yyyyMMdd")
    val from = start match {
      case Some(t) => format.parseDateTime(t)
      case None => metric.getDefaultStart
    }
    val dataPoints = metricQuerySrv.getTop25(metricId, from)
    val dataSeries = new DataSeries(
      name = metric.getName,
      description = metric.getDescription,
      headers = List("Objects", "All"),
      values = dataPoints.map(m => List(m.getX(), m.getY())).toList
    )
    Ok(views.html.hbarchart(dataSeries))
  }

  def line(metricId: String, start: Option[String], end: Option[String],
      interval: Option[String], stat: Option[String]) = Action {

    val metric = ApplicationContext.getMetric(metricId, MetricType.TIME_SERIES)

    val format = DateTimeFormat.forPattern("yyyyMMddHH")
    val from = start match {
      case Some(t) => format.parseDateTime(t)
      case None => metric.getDefaultStart
    }
    val to = end match {
      case Some(t) => format.parseDateTime(t)
      case None => metric.getDefaultEnd()
    }
    val a = interval match {
      case Some(aggr) => Aggregation.valueOf(aggr)
      case None => metric.getDefaultAggregation
    }
    val s = stat match {
      case Some(a) => Statistic.valueOf(a)
      case None => Statistic.avg
    }

    val dataPoints = metricQuerySrv.getTimeSeries(metricId, from, to, s, a)
    val dataSeries = new DataSeries(
      name = metric.getName,
      description = metric.getDescription,
      headers = List("Timestamp", s.name()),
      values = dataPoints.map(m => List(
          m.getX(),
          m.getY())).toList
    )
    Ok(views.html.linechart(dataSeries))
  }
}
