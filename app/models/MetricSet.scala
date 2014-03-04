package models

import scala.language.postfixOps
import scala.collection.JavaConversions.asScalaBuffer
import org.joda.time.DateTime
import org.sagebionetworks.dashboard.model.{Interval, Statistic}
import org.sagebionetworks.dashboard.service.{CountDataPointConverter, EntityIdToName, UserIdToName}
import org.sagebionetworks.dashboard.service.MetricReader
import context.SpringContext

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

    MetricHandle("bar", "uniqueUser") -> MetricSet(
      name = "Count of Unique Users",
      description = "The number of unique users who have used Synapse.",
      start = 7,
      end = 0,
      interval = Interval.day,
      statistic = Statistic.n,
      dataSet = (start, end, interval, statistic) => {
        val data = metricReader.getUniqueCount("uniqueUser", start, end);
        DataSet(
          xLabel = Some("date & time"),
          yLabel = Some("count"),
          xHeader = DataHeader.Timestamp,
          xHeaders = List(DataHeader.Timestamp),
          xValues = List(data map(d => d.getX) toList),
          yHeaders = List("count"),
          yValues = List(data map (d => d.getY) toList)
        )
      }
    ),

    MetricHandle("hbar", "topUser") -> MetricSet(
      name = "Top Users",
      description = "Users who have registered the most activitities.",
      start = 1,
      end = 1,
      interval = Interval.day,
      statistic = Statistic.n,
      dataSet = (start, end, interval, statistic) => {
        val baseUrl = "https://www.synapse.org/#!Profile:"
        val data = metricReader.getTop("uniqueUser", start, 50)
        DataSet(
          xLabel = None,
          yLabel = None,
          xHeader = DataHeader.Name,
          xHeaders = List(DataHeader.ID, DataHeader.URL, DataHeader.Name),
          xValues = List(
            data map (d => d.getX) toList,
            data map (d => baseUrl + d.getX) toList,
            data map (d => userIdToName.convert(d).getX) toList
          ),
          yHeaders = List("count"),
          yValues = List(data map (d => d.getY) toList)
        )
      }
    ),

    MetricHandle("line", "postEntityHeader") -> MetricSet(
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
          xLabel = Some("date and time"),
          yLabel = Some("latency (ms)"),
          xHeader = DataHeader.Timestamp,
          xHeaders = List(DataHeader.Timestamp),
          xValues = List(timeseries map (d => d.getX) toList),
          yHeaders = List("count"),
          yValues = List(timeseries map (d => d.getY) toList)
        )
      }
    )
  )

  def metricSet(handle: MetricHandle) = {
    map(handle)
  }
}
