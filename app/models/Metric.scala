package models

import org.joda.time.DateTime
import org.sagebionetworks.dashboard.model.{Aggregation, Statistic}
import org.sagebionetworks.dashboard.service.{CountDataPointConverter, EntityIdToName, UserIdToName}
import context.AppContext

/**
 * @param fullName     The descriptive name of the metric
 * @param description  Detailed description of the metric
 * @param aggregation  Default aggregation
 * @param statistic    Default statistic
 * @param startOffset  Offset of the starting time in days
 * @param endOffset    Offset of the ending time in days
 */
case class Metric(
  fullName: String,
  description: String,
  aggregation: Aggregation,
  statistic: Statistic,
  startOffset: Int,
  endOffset: Int,
  converters: List[CountDataPointConverter]
)

object Metric {

  private val entityIdToName = AppContext.getBean(classOf[EntityIdToName])
  private val userIdToName = AppContext.getBean(classOf[UserIdToName])

  val metrics = Map(

      MetricId("bar", "uniqueUser") -> Metric(
        "Daily Unique Users",
        "The number of unique users that logged activities on a daily basis.",
        Aggregation.day,
        Statistic.n,
        8,
        1,
        List.empty),

      MetricId("hbar", "uniqueUser") -> Metric(
        "Top Users",
        "Users registered the most activitities.",
        Aggregation.day,
        Statistic.n,
        1,
        1,
        List(userIdToName)),

      MetricId("hbar", "topEntity") -> Metric(
        "Top Entities",
        "List of entities that are accessed most often.",
        Aggregation.day,
        Statistic.n,
        1,
        1,
        List(entityIdToName)),

      MetricId("hbar", "topMethod") -> Metric(
        "Top REST APIs",
        "Most accessed REST APIs.",
        Aggregation.day,
        Statistic.n,
        1,
        1,
        List.empty),

      MetricId("hbar", "topClient") -> Metric(
        "Top Clients",
        "List of programatic clients sorted in descending order of their activities.",
        Aggregation.day,
        Statistic.n,
        1,
        1,
        List.empty),

      MetricId("hbar", "topWebClient") -> Metric(
        "Top Web Clients",
        "List of programatic clients sorted in descending order of their activities.",
        Aggregation.day,
        Statistic.n,
        1,
        1,
        List.empty),

      MetricId("hbar", "topPythonClient") -> Metric(
        "Top Python Clients",
        "List of programatic clients sorted in descending order of their activities.",
        Aggregation.day,
        Statistic.n,
        1,
        1,
        List.empty),

      MetricId("hbar", "topRClient") -> Metric(
        "Top R Clients",
        "List of programatic clients sorted in descending order of their activities.",
        Aggregation.day,
        Statistic.n,
        1,
        1,
        List.empty),

      MetricId("bar", "errorCount") -> Metric(
        "Counts of Errors",
        "Daily counts of errors.",
        Aggregation.day,
        Statistic.n,
        8,
        1,
        List.empty),

      MetricId("line", "getEntityBundle") -> Metric(
        "GET Entity Bundle Latencies",
        "Latency in milliseconds for the GET entity bundle REST API.",
        Aggregation.hour,
        Statistic.avg,
        4,
        1,
        List.empty),

      MetricId("line", "query") -> Metric(
        "Query Latencies",
        "Latency in milliseconds for the query API.",
        Aggregation.hour,
        Statistic.avg,
        4,
        1,
        List.empty)
  )

  def getMetric(metricId: MetricId) = {
    metrics(metricId)
  }
}
