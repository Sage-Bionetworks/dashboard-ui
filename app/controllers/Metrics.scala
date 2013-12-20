package controllers

import play.api.mvc.{Action, Controller}
import models.Metric

object Metrics extends Controller {
  def all = Action {
    Ok(views.html.metrics(Metric.metrics))
  }
}
