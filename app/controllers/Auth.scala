package controllers

import collection.JavaConversions._
import play.api.mvc._
import org.openid4java.consumer.ConsumerManager
import org.openid4java.message.AuthRequest
import org.openid4java.message.AuthSuccess
import org.openid4java.message.ParameterList
import org.openid4java.message.ax.AxMessage
import org.openid4java.message.ax.FetchRequest
import org.openid4java.message.ax.FetchResponse

object Auth extends Controller with Security {

  /**
   * Authenticates and authorizes.
   */
  def auth = AuthorizedAction { implicit request =>
    Ok("You are authorized to access Synapse dashboard.")
  }
}
