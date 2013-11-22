package models

import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper

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
