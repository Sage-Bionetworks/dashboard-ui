package models

import org.joda.time.DateTime
import org.sagebionetworks.dashboard.model.{Aggregation, Statistic}
import org.sagebionetworks.dashboard.service.{CountDataPointConverter, EntityIdToName, UserIdToName}
import context.SpringContext

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
  converters: List[CountDataPointConverter],
  url: Option[String]
)

object Metric {

  private val entityIdToName = SpringContext.getBean(classOf[EntityIdToName])
  private val userIdToName = SpringContext.getBean(classOf[UserIdToName])

  val metrics = collection.immutable.ListMap(

      MetricId("bar", "uniqueUser") -> Metric(
        "Daily Unique Users",
        "The number of unique users that logged activities on a daily basis.",
        Aggregation.day,
        Statistic.n,
        7,
        0,
        List.empty,
        None),

      MetricId("hbar", "uniqueUser") -> Metric(
        "Top Users",
        "Users registered the most activitities.",
        Aggregation.day,
        Statistic.n,
        1,
        1,
        List(userIdToName),
        Some("https://www.synapse.org/#!Profile:")),

      MetricId("hbar", "topEntity") -> Metric(
        "Top Entities",
        "List of entities that are accessed most often.",
        Aggregation.day,
        Statistic.n,
        1,
        1,
        List(entityIdToName),
        Some("https://www.synapse.org/#!Synapse:")),

      MetricId("hbar", "topMethod") -> Metric(
        "Top REST APIs",
        "Most accessed REST APIs.",
        Aggregation.day,
        Statistic.n,
        1,
        1,
        List.empty,
        None),

      MetricId("hbar", "topClient") -> Metric(
        "Top Clients",
        "List of programatic clients sorted in descending order of their activities.",
        Aggregation.day,
        Statistic.n,
        1,
        1,
        List.empty,
        None),

      MetricId("hbar", "topWebClient") -> Metric(
        "Top Web Clients",
        "List of programatic clients sorted in descending order of their activities.",
        Aggregation.day,
        Statistic.n,
        1,
        1,
        List.empty,
        None),

      MetricId("hbar", "topPythonClient") -> Metric(
        "Top Python Clients",
        "List of programatic clients sorted in descending order of their activities.",
        Aggregation.day,
        Statistic.n,
        1,
        1,
        List.empty,
        None),

      MetricId("hbar", "topRClient") -> Metric(
        "Top R Clients",
        "List of programatic clients sorted in descending order of their activities.",
        Aggregation.day,
        Statistic.n,
        1,
        1,
        List.empty,
        None),

      MetricId("line", "postEntityHeader") -> Metric(
        "POST Entity Header Latencies",
        "Latency in milliseconds for the POST entity header REST API.",
        Aggregation.hour,
        Statistic.avg,
        7,
        0,
        List.empty,
        None),

      MetricId("line", "getEntityBundle") -> Metric(
        "GET Entity Bundle Latencies",
        "Latency in milliseconds for the GET entity bundle REST API.",
        Aggregation.hour,
        Statistic.avg,
        7,
        0,
        List.empty,
        None),

      MetricId("line", "query") -> Metric(
        "Query Latencies",
        "Latency in milliseconds for the query API.",
        Aggregation.hour,
        Statistic.avg,
        7,
        0,
        List.empty,
        None)
  )

  def getMetric(metricId: MetricId) = {
    metrics(metricId)
  }
}
