package models

import scala.language.postfixOps
import scala.collection.JavaConversions.asScalaBuffer
import org.joda.time.DateTime
import org.sagebionetworks.dashboard.model.{Interval, Statistic}
import org.sagebionetworks.dashboard.service.{CountDataPointConverter, EntityIdToName, UserIdToName}
import org.sagebionetworks.dashboard.service.MetricReader
import context.SpringContext
import MetricType._

/**
 * One set of metrics. This is a template to query a set of metrics and to assemble a data set.
 *
 * @param name         The descriptive name of this metric set
 * @param description  Short description of this metric set
 * @param interval     Default aggregation interval
 * @param statistic    Optional default statistic
 * @param start        Offset in days of the starting time
 * @param end          Offset in days of the ending time
 * @param dataSet      Function to generate the data set for this metric set
 */
case class MetricSet(
  name: String,
  description: String,
  start: Int,
  end: Int,
  interval: Interval,
  statistic: Statistic,
  dataSet: (DateTime, DateTime, Interval, Statistic) => DataSet
)

object MetricSet {

  private val metricReader = SpringContext.getBean(classOf[MetricReader])
  private val entityIdToName = SpringContext.getBean(classOf[EntityIdToName])
  private val userIdToName = SpringContext.getBean(classOf[UserIdToName])

  val map = collection.immutable.ListMap(

    MetricHandle(ActiveUser, "user") -> MetricSet(
      name = "Count of Active Users",
      description = "The number of active users who have used Synapse at least 3 days per month.",
      start = 210,
      end = 0,
      interval = Interval.month,
      statistic = Statistic.n,
      dataSet = (start, end, interval, statistic) => {
        val data = metricReader.getUniqueCount("activeUser", interval, start, end, 3L, Long.MaxValue);
        DataSet(
          xLabel = Some("date"),
          yLabel = Some("count of active users"),
          xHeaders = List(DataHeader.Timestamp),
          xValues = List(data map(d => d.x) toList),
          yHeaders = List("all users"),
          yValues = List(data map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(Unique, "user") -> MetricSet(
      name = "Count of Unique Users",
      description = "The number of unique users who have used Synapse.",
      start = 7,
      end = 0,
      interval = Interval.day,
      statistic = Statistic.n,
      dataSet = (start, end, interval, statistic) => {
        val data = metricReader.getUniqueCount("uniqueUser", interval, start, end);
        DataSet(
          xLabel = Some("date"),
          yLabel = Some("count of unique users"),
          xHeaders = List(DataHeader.Timestamp),
          xValues = List(data map(d => d.x) toList),
          yHeaders = List("all users"),
          yValues = List(data map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(Top, "user") -> MetricSet(
      name = "Top Users (Session Count)",
      description = "Top 20 users who have registered the most activitities.",
      start = 1,
      end = 1,
      interval = Interval.month,
      statistic = Statistic.n,
      dataSet = (start, end, interval, statistic) => {
        val baseUrl = "https://www.synapse.org/#!Profile:"
        val data = metricReader.getTop("uniqueUser", interval, start, 0, 20)
        DataSet(
          xLabel = None,
          yLabel = None,
          xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL),
          xValues = List(
            data map (d => userIdToName.convert(d).x) toList,
            data map (d => d.x) toList,
            data map (d => baseUrl + d.x) toList
          ),
          yHeaders = List("count"),
          yValues = List(data map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(TopByDay, "user") -> MetricSet(
      name = "Top Users (Day Count)",
      description = "Top 20 users who have registered the most days.",
      start = 1,
      end = 1,
      interval = Interval.month,
      statistic = Statistic.n,
      dataSet = (start, end, interval, statistic) => {
        val baseUrl = "https://www.synapse.org/#!Profile:"
        val data = metricReader.getTop("activeUser", interval, start, 0, 20)
        DataSet(
          xLabel = None,
          yLabel = None,
          xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL),
          xValues = List(
            data map (d => userIdToName.convert(d).x) toList,
            data map (d => d.x) toList,
            data map (d => baseUrl + d.x) toList
          ),
          yHeaders = List("count"),
          yValues = List(data map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(Top, "entity") -> MetricSet(
      name = "Top Entities (Session Count)",
      description = "List of the top 20 most accessed entities.",
      start = 1,
      end = 1,
      interval = Interval.month,
      statistic = Statistic.n,
      dataSet = (start, end, interval, statistic) => {
        val baseUrl = "https://www.synapse.org/#!Synapse:"
        val data = metricReader.getTop("topEntity", interval, start, 0, 20)
        DataSet(
          xLabel = None,
          yLabel = None,
          xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL),
          xValues = List(
            data map (d => entityIdToName.convert(d).x) toList,
            data map (d => d.x) toList,
            data map (d => baseUrl + d.x) toList
          ),
          yHeaders = List("count"),
          yValues = List(data map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(Top, "fileDownload") -> MetricSet(
      name = "Top File Downloads (Session Count)",
      description = "List of the top 20 most downloaded files.",
      start = 1,
      end = 1,
      interval = Interval.month,
      statistic = Statistic.n,
      dataSet = (start, end, interval, statistic) => {
        val baseUrl = "https://www.synapse.org/#!Synapse:"
        val data = metricReader.getTop("fileDownload", interval, start, 0, 20)
        DataSet(
          xLabel = None,
          yLabel = None,
          xHeaders = List(DataHeader.Name, DataHeader.ID, DataHeader.URL),
          xValues = List(
            data map (d => entityIdToName.convert(d).x) toList,
            data map (d => d.x) toList,
            data map (d => baseUrl + d.x) toList
          ),
          yHeaders = List("count"),
          yValues = List(data map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(Top, "method") -> MetricSet(
      name = "Top REST APIs (Session Count)",
      description = "List of the top 20 most accessed REST APIs.",
      start = 1,
      end = 1,
      interval = Interval.month,
      statistic = Statistic.n,
      dataSet = (start, end, interval, statistic) => {
        val data = metricReader.getTop("topMethod", interval, start, 0, 20)
        DataSet(
          xLabel = None,
          yLabel = None,
          xHeaders = List(DataHeader.Name),
          xValues = List(
            data map (d => {
              // Pad the HTTP method name to align the URLs after it
              val parts = d.x.split(" ")
              parts(0).toUpperCase.padTo(5, ' ') + parts(1)
            }) toList
          ),
          yHeaders = List("count"),
          yValues = List(data map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(Top, "client") -> MetricSet(
      name = "Top Clients (Session Count)",
      description = "List of the top 20 clients.",
      start = 1,
      end = 1,
      interval = Interval.month,
      statistic = Statistic.n,
      dataSet = (start, end, interval, statistic) => {
        val data = metricReader.getTop("topClient", interval, start, 0, 20)
        DataSet(
          xLabel = None,
          yLabel = None,
          xHeaders = List(DataHeader.Name),
          xValues = List(
            data map (d => d.x) toList
          ),
          yHeaders = List("count"),
          yValues = List(data map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(Top, "python") -> MetricSet(
      name = "Top Python Clients",
      description = "List of the top 20 Python clients.",
      start = 1,
      end = 1,
      interval = Interval.month,
      statistic = Statistic.n,
      dataSet = (start, end, interval, statistic) => {
        val data = metricReader.getTop("topPythonClient", interval, start, 0, 20)
        DataSet(
          xLabel = None,
          yLabel = None,
          xHeaders = List(DataHeader.Name),
          xValues = List(
            data map (d => d.x) toList
          ),
          yHeaders = List("count"),
          yValues = List(data map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(Top, "r") -> MetricSet(
      name = "Top R Clients",
      description = "List of the top 20 R clients.",
      start = 1,
      end = 1,
      interval = Interval.month,
      statistic = Statistic.n,
      dataSet = (start, end, interval, statistic) => {
        val data = metricReader.getTop("topRClient", interval, start, 0, 20)
        DataSet(
          xLabel = None,
          yLabel = None,
          xHeaders = List(DataHeader.Name),
          xValues = List(
            data map (d => d.x) toList
          ),
          yHeaders = List("count"),
          yValues = List(data map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(Top, "web") -> MetricSet(
      name = "Top Web Clients",
      description = "List of the top 20 web clients.",
      start = 1,
      end = 1,
      interval = Interval.month,
      statistic = Statistic.n,
      dataSet = (start, end, interval, statistic) => {
        val data = metricReader.getTop("topWebClient", interval, start, 0, 20)
        DataSet(
          xLabel = None,
          yLabel = None,
          xHeaders = List(DataHeader.Name),
          xValues = List(
            data map (d => d.x) toList
          ),
          yHeaders = List("count"),
          yValues = List(data map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(Latency, "global") -> MetricSet(
      name = "Global Latencies",
      description = "Latency in milliseconds for all the REST APIs.",
      start = 7,
      end = 0,
      interval = Interval.hour,
      statistic = Statistic.avg,
      dataSet = (start, end, interval, statistic) => {
        val timeseries = metricReader.getTimeSeries("globalLatency", start, end,
            statistic, interval)
        DataSet(
          xLabel = Some("time"),
          yLabel = Some("latency (ms)"),
          xHeaders = List(DataHeader.Timestamp),
          xValues = List(timeseries map (d => d.x) toList),
          yHeaders = List("ALL"),
          yValues = List(timeseries map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(Latency, "postEntityHeader") -> MetricSet(
      name = "POST-Entity-Header Latencies",
      description = "Latency in milliseconds for the POST-entity-header REST API.",
      start = 7,
      end = 0,
      interval = Interval.hour,
      statistic = Statistic.avg,
      dataSet = (start, end, interval, statistic) => {
        val timeseries = metricReader.getTimeSeries("postEntityHeader", start, end,
            statistic, interval)
        DataSet(
          xLabel = Some("time"),
          yLabel = Some("latency (ms)"),
          xHeaders = List(DataHeader.Timestamp),
          xValues = List(timeseries map (d => d.x) toList),
          yHeaders = List("PEH"),
          yValues = List(timeseries map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(Latency, "getEntityBundle") -> MetricSet(
      name = "GET-Entity-Bundle Latencies",
      description = "Latency in milliseconds for the GET-entity-bundle REST API.",
      start = 7,
      end = 0,
      interval = Interval.hour,
      statistic = Statistic.avg,
      dataSet = (start, end, interval, statistic) => {
        val timeseries = metricReader.getTimeSeries("getEntityBundle", start, end,
            statistic, interval)
        DataSet(
          xLabel = Some("time"),
          yLabel = Some("latency (ms)"),
          xHeaders = List(DataHeader.Timestamp),
          xValues = List(timeseries map (d => d.x) toList),
          yHeaders = List("GEB"),
          yValues = List(timeseries map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(Latency, "query") -> MetricSet(
      name = "Query Latencies",
      description = "Latency in milliseconds for the query REST API.",
      start = 7,
      end = 0,
      interval = Interval.hour,
      statistic = Statistic.avg,
      dataSet = (start, end, interval, statistic) => {
        val timeseries = metricReader.getTimeSeries("query", start, end,
            statistic, interval)
        DataSet(
          xLabel = Some("time"),
          yLabel = Some("latency (ms)"),
          xHeaders = List(DataHeader.Timestamp),
          xValues = List(timeseries map (d => d.x) toList),
          yHeaders = List("QRY"),
          yValues = List(timeseries map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(Latency, "search") -> MetricSet(
      name = "Search Latencies",
      description = "Latency in milliseconds for the search REST API.",
      start = 7,
      end = 0,
      interval = Interval.hour,
      statistic = Statistic.avg,
      dataSet = (start, end, interval, statistic) => {
        val timeseries = metricReader.getTimeSeries("search", start, end,
            statistic, interval)
        DataSet(
          xLabel = Some("time"),
          yLabel = Some("latency (ms)"),
          xHeaders = List(DataHeader.Timestamp),
          xValues = List(timeseries map (d => d.x) toList),
          yHeaders = List("SRCH"),
          yValues = List(timeseries map (d => d.y) toList)
        )
      }
    ),

    MetricHandle(Latency, "desc") -> MetricSet(
      name = "GET-Descendants Latencies",
      description = "Latency in milliseconds for the GET-descendants REST API.",
      start = 7,
      end = 0,
      interval = Interval.hour,
      statistic = Statistic.avg,
      dataSet = (start, end, interval, statistic) => {
        val timeseries = metricReader.getTimeSeries("getDescendants", start, end,
            statistic, interval)
        DataSet(
          xLabel = Some("time"),
          yLabel = Some("latency (ms)"),
          xHeaders = List(DataHeader.Timestamp),
          xValues = List(timeseries map (d => d.x) toList),
          yHeaders = List("DESC"),
          yValues = List(timeseries map (d => d.y) toList)
        )
      }
    )
  )

  def metricSet(handle: MetricHandle) = {
    map(handle)
  }
}
