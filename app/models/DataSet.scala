package models

import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper

/**
 * Data for rendering a chart. It is organized into two dimensions.
 * The x dimension has the metadata (data about the actual data).
 * Examples include timestamps, ids, names, URIs. The y dimension
 * is the actual data which will be plotted on the chart.
 * <p>
 * Data are compacted into CSV-like structure. Each dimension is composed of
 * a list of column headers and a data matrix. The entire data set can be
 * converted into a JSON object to be consumed by HTTP clients.
 *
 * @param xLabel    Optional label for the x-axis
 * @param yLabel    Optional label for the y-axis
 * @param xHeader   The header for the x column
 * @param xHeaders  The list of headers for the x dimension
 * @param xValues   The value matrix for the x dimension
 * @param yHeaders  The list of headers for the y dimension
 * @param yValues   The value matrix for the y dimension
 */
case class DataSet (
  xLabel: Option[String],
  yLabel: Option[String],
  xHeader: DataHeader.Value,
  xHeaders: List[DataHeader.Value],
  xValues: List[List[String]],
  yHeaders: List[String],
  yValues: List[List[String]]
) {
  def json: JsValue = Json.obj(
    "xLabel" -> Json.toJson(xLabel),
    "yLabel" -> Json.toJson(yLabel),
    "xHeader" -> Json.toJson(xHeader.toString),
    "xHeaders" -> Json.toJson(xHeaders map (header => header.toString)),
    "xValues" -> Json.toJson(xValues),
    "yHeaders" -> Json.toJson(yHeaders),
    "yValues" -> Json.toJson(yValues)
  )
}
