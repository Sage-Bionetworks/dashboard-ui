package controllers

import play.api.mvc.Controller
import models.MetricSet

object Metrics extends Controller with Security {
  def all = AuthorizedAction {
    Ok(views.html.metrics(MetricSet.map))
  }
}
