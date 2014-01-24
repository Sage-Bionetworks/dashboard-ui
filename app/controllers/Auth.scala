package controllers

import play.api.mvc.Controller

object Auth extends Controller with Security {

  /**
   * Authenticates and authorizes.
   */
  def auth = AuthorizedAction { implicit request =>
    Ok("You are authorized to access Synapse dashboard.")
  }
}
