package controllers

import scala.collection.JavaConversions.asScalaBuffer
import org.joda.time.DateTime
import play.api.mvc.{Action, Controller}
import org.sagebionetworks.dashboard.model.{Interval, Statistic}
import org.sagebionetworks.dashboard.service.MetricReader
import models.{MetricHandle, MetricSet}
import context.SpringContext

object Charts extends Controller with Security {

  private val metricReader = SpringContext.getBean(classOf[MetricReader])

  def chart(chartType: String, metricName: String) = AuthorizedAction {
    val metricSet = MetricSet.metricSet(MetricHandle(chartType, metricName))
    val title = metricSet.name
    val desc = metricSet.description
    val start = Some(DateTime.now.minusDays(metricSet.start).getMillis.toString)
    val end = Some(DateTime.now.minusDays(metricSet.end).getMillis.toString)
    val interval = Some(metricSet.interval.name)
    val stat = Some(metricSet.statistic.name)
    Ok(views.html.chart(chartType, metricName, title, desc, start, end, interval, stat))
  }

  def data(chartType: String, metricName: String, start: Option[String], end: Option[String],
      interval: Option[String], statistic: Option[String]) = AuthorizedAction {
    val metricSet = MetricSet.metricSet(MetricHandle(chartType, metricName))
    val from = start match {
      case Some(sth) => new DateTime(sth.toLong)
      case None => DateTime.now.minusDays(metricSet.start)
    }
    val to = end match {
      case Some(sth) => new DateTime(sth.toLong)
      case None => DateTime.now.minusDays(metricSet.end)
    }
    val intvl = interval match {
      case Some(sth) => Interval.valueOf(sth)
      case None => metricSet.interval
    }
    val stat = statistic match {
      case Some(sth) => Statistic.valueOf(sth)
      case None => metricSet.statistic
    }
    Ok(metricSet.dataSet(from, to, intvl, stat).json)
  }
}
