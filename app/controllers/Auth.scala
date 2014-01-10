package controllers

import play.api.mvc._

import org.openid4java.consumer.ConsumerManager
import org.openid4java.message.AuthRequest
import org.openid4java.message.ax.FetchRequest

object Auth extends Controller {

  private val providerUrl = "https://www.google.com/accounts/o8/id"
  private val openIdMgr = new ConsumerManager()
  private val discovered = openIdMgr.associate(openIdMgr.discover(providerUrl))

  /**
   * Logs in the user with Google OpenID 2.0.
   */
  def login = Action { implicit request =>
    val returnToUrl = routes.Auth.authenticate.absoluteURL(false)
    val authReq = openIdMgr.authenticate(discovered, returnToUrl)
    val fetchReq = FetchRequest.createFetchRequest
    fetchReq.addAttribute(
        "email",
        "http://schema.openid.net/contact/email",
        true)
    authReq.addExtension(fetchReq)
    Redirect(authReq.getDestinationUrl(true))
  }

  /**
   * Authenticates based on the 'sagebase.org' email domain.
   */
  def authenticate = Action { request =>
    Ok("authenticate")
  }

  /**
   * Authorizes based on Synapse user info.
   */
  def authorize = Action { request =>
    Ok("authorize")
  }
}
