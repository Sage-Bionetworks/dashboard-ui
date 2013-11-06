package controllers

import scala.collection.JavaConversions._
import play.api.mvc.{Action, Controller}
import org.sagebionetworks.dashboard.service.NameIdService
import models.Metric

object Metrics extends Controller {

  private val nameIdService = ApplicationContext.getBean(classOf[NameIdService])

  def all = Action {
    val names = nameIdService.getMetrics
    val metrics = names.map(name => Metric(nameIdService.getMetricId(name), name, name)).toList
    Ok(views.html.metrics(metrics))
  }

  def prefix(prefix: String) = TODO
}
