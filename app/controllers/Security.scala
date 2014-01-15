package controllers

import scala.collection.JavaConversions.mapAsJavaMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.duration.HOURS

import org.openid4java.consumer.ConsumerManager
import org.openid4java.message.AuthSuccess
import org.openid4java.message.ParameterList
import org.openid4java.message.ax.AxMessage
import org.openid4java.message.ax.FetchRequest
import org.openid4java.message.ax.FetchResponse

import play.api.Play.current
import play.api.cache.Cache
import play.api.mvc.ActionBuilder
import play.api.mvc.Request
import play.api.mvc.Results.Redirect
import play.api.mvc.Results.Unauthorized
import play.api.mvc.SimpleResult

import org.sagebionetworks.dashboard.service.SecurityService

import context.AppContext

trait Security {

  object AuthorizedAction extends ActionBuilder[Request] {

    private object GoogleOpenId {
      val ProviderUrl = "https://www.google.com/accounts/o8/id"
      val Manager = new ConsumerManager
      val Discovered = Manager.associate(Manager.discover(ProviderUrl))
      val CallbackQueryParameter = "openid.mode"
    }

    private val tokenKey = "token"
    private val tokenExpire = 3        // Expire after 3 hours on the server side
    private val protocol = "http://"

    private val securityService = AppContext.getBean(classOf[SecurityService])

    def invokeBlock[A](request: Request[A], block: Request[A] => Future[SimpleResult]) = {
      request.session.get(tokenKey).map { token =>
        Cache.get(token).map { t =>
          // Session tokens match => continue with the request
          block(request).map { result =>
            // Save the session token in the response
            result.withSession(tokenKey -> token)
          }
        } getOrElse {
          // Missing session on the server side
          login(request, block)
        }
      } getOrElse {
        // Missing session in the request
        login(request, block)
      }
    }

    def login[A](request: Request[A], block: Request[A] => Future[SimpleResult]) = {
      // Check if this is OpenID callback
      request.queryString.get(GoogleOpenId.CallbackQueryParameter) match {
        case Some(p) => openIdCallback(request, block)
        case None => openIdLogin(request) // When this is not a callback, log in 
      }
    }

    /**
     * Verifies the OpenID login and reads the user email address.
     */
    def openIdCallback[A](request: Request[A], block: Request[A] => Future[SimpleResult]) = {
      val receivingURL = protocol + request.host + request.uri
      val parameterMap = new ParameterList(request.queryString.map { case (k,v) => k -> v.mkString })
      val verification = GoogleOpenId.Manager.verify(
          receivingURL.toString(),
          parameterMap,
          GoogleOpenId.Discovered)
      val verifiedId = verification.getVerifiedId()
      if (verifiedId == null) {
        // Invalid callback
        openIdLogin(request)
      } else {
        val authSuccess = verification.getAuthResponse().asInstanceOf[AuthSuccess]
        val fetchResp = authSuccess.getExtension(AxMessage.OPENID_NS_AX).asInstanceOf[FetchResponse]
        val emails = fetchResp.getAttributeValues("email")
        val email = emails.get(0).toString
        if (email == null) {
          Future.successful(Unauthorized("Missing email address from the login."))
        } else if (isAuthorized(email)) {
          // Save the session on the server side
          val session = java.util.UUID.randomUUID.toString
          Cache.set(session, session, Duration(tokenExpire, HOURS))
          // Continue with the original request
          // and save the session token in the response
          block(request).map { result =>
            result.withSession(tokenKey -> session)
          }
        } else {
          Future.successful(Unauthorized(email + " is not authorized to access dashboard."))
        }
      }
    }

    /**
     * Logs in the user using Google OpenID 2.0.
     */
    def openIdLogin[A](request: Request[A]) = {
      val returnToUrl = protocol + request.host + request.uri
      val authReq = GoogleOpenId.Manager.authenticate(GoogleOpenId.Discovered, returnToUrl)
      val fetchReq = FetchRequest.createFetchRequest
      fetchReq.addAttribute(
          "email",
          "http://schema.openid.net/contact/email",
          true)
      authReq.addExtension(fetchReq)
      Future.successful(Redirect(authReq.getDestinationUrl(true)))
    }

    /**
     * Authorizes based on Synapse user info.
     */
    def isAuthorized(email: String) = {
      securityService.isDashboardUser(email)
    }
  }
}
