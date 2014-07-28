package controllers

import scala.language.postfixOps
import scala.collection.JavaConversions.asScalaBuffer
import org.joda.time.DateTime
import play.api.mvc.{Action, Controller}
import org.sagebionetworks.dashboard.model.{Interval, Statistic}
import org.sagebionetworks.dashboard.service.MetricReader
import models.{MetricHandle, MetricSet, MetricType}
import context.SpringContext

object Charts extends Controller with Security {

  def chart(metricType: String, metricName: String) = AuthorizedAction {
    val mType = MetricType.withName(metricType)
    val metricSet = MetricSet.findMetric(MetricHandle(mType, metricName)) get
    val title = metricSet.name
    val desc = metricSet.description
    val start = Some(DateTime.now.minusDays(metricSet.start).getMillis.toString)
    val end = Some(DateTime.now.minusDays(metricSet.end).getMillis.toString)
    val interval = Some(metricSet.interval.name)
    val stat = Some(metricSet.statistic.name)
    Ok(views.html.chart(mType, metricName, title, desc, start, end, interval, stat))
  }

  def data(metricType: String, metricName: String, start: Option[String], end: Option[String],
      interval: Option[String], statistic: Option[String], page: Option[Int], text: Option[String]) = AuthorizedAction {
    val mType = MetricType.withName(metricType)
    val metricSet = MetricSet.findMetric(MetricHandle(mType, metricName)) get
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
    val p = page match {
      case Some(sth) => sth
      case None => 0
    }
    val t = text match {
      case Some(str) => str
      case None => ""
    }
    Ok(metricSet.dataSet(from, to, intvl, stat, p, t).json)
  }
}
