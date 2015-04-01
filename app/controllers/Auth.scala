package controllers

import play.api.mvc.Action
import play.api.mvc.Controller

import org.sagebionetworks.dashboard.config.DashboardConfig
import context.SpringContext

object Auth extends Controller {

  private object Whitelist {
    val whitelist = SpringContext.getBean(
        classOf[DashboardConfig]).getUserWhitelist.split(":").toSet
    def contains(item: String) = {
      whitelist.contains(item)
    }
  }

  /**
   * Authenticates and authorizes.
   */
  def googleOAuth2 = Action { implicit request =>
    Ok(request.queryString)
  }

  private def isAuthorizedByEmail(email: String) = {
    email != null && (email.toLowerCase().endsWith("sagebase.org") || Whitelist.contains(email));
  }

  private def isAuthorizedById(id: String) = {
    id != null && Whitelist.contains(id);
  }
}
