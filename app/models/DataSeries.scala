package models

import play.api.libs.json._

case class DataSeries (
  name: String, description: String,
  headers: List[String], values: List[List[String]]
) {
  def toJson: JsValue = Json.obj(
    "name" -> name,
    "headers" -> Json.toJson(headers),
    "values" -> Json.toJson(values)
  )
}
