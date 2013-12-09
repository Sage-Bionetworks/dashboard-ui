package controllers

import scala.collection.JavaConversions.asScalaBuffer

import org.joda.time.DateTime
import org.sagebionetworks.dashboard.model.{Aggregation, MetricType, Statistic}
import org.sagebionetworks.dashboard.service.MetricQueryService

import play.api.mvc.{Action, Controller}

import models.DataSeries

object Charts extends Controller {

  private val metricQuerySrv = ApplicationContext.getBean(classOf[MetricQueryService])

  def chart(metricType: String, metricId: String) = Action {
    val mType = metricType match {
      case "bar" => MetricType.UNIQUE_COUNT
      case "hbar" => MetricType.TOP
      case "line" => MetricType.TIME_SERIES
    }
    val metric = ApplicationContext.getMetric(metricId, mType)
    val metricName = metric.getName
    val metricDesc = metric.getDescription
    val start = Option(metric.getDefaultStart.getMillis.toString)
    val end = Option(metric.getDefaultEnd.getMillis.toString)
    val interval = Option(metric.getDefaultAggregation.name)
    val stat = Option(metric.getDefaultStatistic.name)
    Ok(views.html.chart(metricType, metricId, metricName, metricDesc, start, end, interval, stat))
  }

  def data(metricType: String, metricId: String, start: Option[String], end: Option[String],
      interval: Option[String], stat: Option[String]) = Action {

    val mType = metricType match {
      case "bar" => MetricType.UNIQUE_COUNT
      case "hbar" => MetricType.TOP
      case "line" => MetricType.TIME_SERIES
    }
    
    val metric = ApplicationContext.getMetric(metricId, mType)

    val from = start match {
      case Some(t) => new DateTime(t.toLong)
      case None => metric.getDefaultStart
    }
    val to = end match {
      case Some(t) => new DateTime(t.toLong)
      case None => metric.getDefaultEnd()
    }

    val singleMetric = metricId.split(":")(0) // TODO: Temporary code to load only the first metric
    val dataPoints = metricType match {
      case "bar" => metricQuerySrv.getUniqueCount(singleMetric, from, to)
      case "hbar" => metricQuerySrv.getTop25(singleMetric, from)
      case "line" => {
        val a = interval match {
          case Some(aggr) => Aggregation.valueOf(aggr)
          case None => metric.getDefaultAggregation
        }
        val s = stat match {
          case Some(a) => Statistic.valueOf(a)
          case None => Statistic.avg
        }
        metricQuerySrv.getTimeSeries(singleMetric, from, to, s, a)
      }
    }

    val dataSeries = new DataSeries(
      name = metric.getName,
      description = metric.getDescription,
      headers = List("x", ""),
      values = dataPoints.map(m => List(
          m.getX(),
          m.getY())).toList
    )

    Ok(dataSeries.toJson)
  }
}
