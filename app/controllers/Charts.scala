package controllers

import scala.collection.JavaConversions.asScalaBuffer

import org.joda.time.format.DateTimeFormat
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
    val metricName = metric.getName()
    val metricDesc = metric.getDescription()
    metric.getDefaultAggregation()
    metric.getDefaultStatistic()
    metric.getDefaultStart()
    metric.getDefaultEnd()
    Ok(views.html.chart(metricType, metricId, metricName, metricDesc, None, None, None, None))
  }

  def data(metricType: String, metricId: String, start: Option[String], end: Option[String],
      interval: Option[String], stat: Option[String]) = Action {

    val mType = metricType match {
      case "bar" => MetricType.UNIQUE_COUNT
      case "hbar" => MetricType.TOP
      case "line" => MetricType.TIME_SERIES
    }
    
    val metric = ApplicationContext.getMetric(metricId, mType)

    val format = metricType match {
      case "bar" => DateTimeFormat.forPattern("yyyyMMdd")
      case "hbar" => DateTimeFormat.forPattern("yyyyMMdd")
      case "line" => DateTimeFormat.forPattern("yyyyMMddHH")
    }

    val from = start match {
      case Some(t) => format.parseDateTime(t)
      case None => metric.getDefaultStart
    }
    val to = end match {
      case Some(t) => format.parseDateTime(t)
      case None => metric.getDefaultEnd()
    }

    val dataPoints = metricType match {
      case "bar" => metricQuerySrv.getUniqueCount(metricId, from, to)
      case "hbar" => metricQuerySrv.getTop25(metricId, from)
      case "line" => {
        val a = interval match {
          case Some(aggr) => Aggregation.valueOf(aggr)
          case None => metric.getDefaultAggregation
        }
        val s = stat match {
          case Some(a) => Statistic.valueOf(a)
          case None => Statistic.avg
        }
        metricQuerySrv.getTimeSeries(metricId, from, to, s, a)
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
