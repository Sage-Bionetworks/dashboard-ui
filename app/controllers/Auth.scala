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

object Auth extends Controller {

  private val providerUrl = "https://www.google.com/accounts/o8/id"
  private val openIdMgr = new ConsumerManager
  private val discovered = openIdMgr.associate(openIdMgr.discover(providerUrl))

  /**
   * Logs in the user using Google OpenID 2.0.
   */
  def login = Action { implicit request =>
    val returnToUrl = routes.Auth.auth.absoluteURL(false)
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
   * Authenticates and authorizes.
   */
  def auth = Action { implicit request =>
    val receivingURL = "http://" + request.host + request.uri
    val response = new ParameterList(request.queryString.map { case (k,v) => k -> v.mkString })
    val verification = openIdMgr.verify(
        receivingURL.toString(),
        response, discovered)
    val verifiedId = verification.getVerifiedId()
    if (verifiedId == null) {
      Forbidden("Log in failed.")
    } else {
      val authSuccess = verification.getAuthResponse().asInstanceOf[AuthSuccess]
      val fetchResp = authSuccess.getExtension(AxMessage.OPENID_NS_AX).asInstanceOf[FetchResponse]
      val emails = fetchResp.getAttributeValues("email")
      authorize(emails.get(0).toString)
    }
  }

  /**
   * Authorizes based on Synapse user info.
   */
  def authorize(email: String): Result = {
    if (email == null) {
      Unauthorized("Missing email address.")
    } else if (email.toLowerCase().endsWith("@sagebase.org")) {
      // TODO: Cross-check with Synapse for tighter security
      Ok("authorized")
    } else {
      Unauthorized(email + " is not authorized.");
    }
  }
}
