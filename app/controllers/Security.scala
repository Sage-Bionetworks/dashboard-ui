package controllers

import java.util.UUID
import collection.JavaConversions._
import concurrent.Future
import concurrent.ExecutionContext.Implicits._
import play.api.mvc._
import play.api.mvc.Results._
import org.joda.time.DateTime
import org.openid4java.consumer.ConsumerManager
import org.openid4java.message.AuthRequest
import org.openid4java.message.AuthSuccess
import org.openid4java.message.ParameterList
import org.openid4java.message.ax.AxMessage
import org.openid4java.message.ax.FetchRequest
import org.openid4java.message.ax.FetchResponse

trait Security {

  object AuthorizedAction extends ActionBuilder[Request] {

    private val providerUrl = "https://www.google.com/accounts/o8/id"
    private val openIdMgr = new ConsumerManager
    private val discovered = openIdMgr.associate(openIdMgr.discover(providerUrl))

    // TODO: Do this in the cache
    // (session-token -> timestamp)
    private val sessions = scala.collection.mutable.Map[String, DateTime]()

    def invokeBlock[A](request: Request[A], block: Request[A] => Future[SimpleResult]) = {
      request.session.get("session-token").map { session =>
        sessions.get(session).map { timestamp =>
          // Make sure the session has not expired
          if (timestamp.plusHours(3).isAfterNow()) {
            // Continue with the original request
            // and save the session in the response
            block(request).map {result => 
              result.withSession("session-token" -> session)
            }
          } else {
            // Expired session
            openIdCallback(request, block)
          }
        } getOrElse {
          // Missing session in the server
          openIdCallback(request, block)
        }
      } getOrElse {
        // Missing session in the request
        openIdCallback(request, block)
      }
    }

    /**
     * Verifies the OpenID login and reads the user email address.
     */
    def openIdCallback[A](request: Request[A], block: Request[A] => Future[SimpleResult]) = {
      val receivingURL = "http://" + request.host + request.uri
      val response = new ParameterList(request.queryString.map { case (k,v) => k -> v.mkString })
      if (!response.hasParameter("openid.mode")) {
        // Not a callback
        Future.successful(openIdLogin(request))
      } else {
        val verification = openIdMgr.verify(
            receivingURL.toString(),
            response, discovered)
        val verifiedId = verification.getVerifiedId()
        if (verifiedId == null) {
          // Invalid callback
          Future.successful(openIdLogin(request))
        } else {
          val authSuccess = verification.getAuthResponse().asInstanceOf[AuthSuccess]
          val fetchResp = authSuccess.getExtension(AxMessage.OPENID_NS_AX).asInstanceOf[FetchResponse]
          val emails = fetchResp.getAttributeValues("email")
          val email = emails.get(0).toString
          if (email == null) {
            Future.successful(Unauthorized("Missing email address."))
          } else if (isAuthorized(email)) {
            // Save session in server
            val session = java.util.UUID.randomUUID.toString
            sessions.put(session, new DateTime)
            // Continue with the original request
            // and save the session in the response
            block(request).map {result => 
              result.withSession("session-token" -> session)
            }
          } else {
            Future.successful(Unauthorized(email + " is not authorized."))
          }
        }
      }
    }

    /**
     * Logs in the user using Google OpenID 2.0.
     */
    def openIdLogin[A](request: Request[A]) = {
      val returnToUrl = "http://" + request.host + request.uri
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
     * Authorizes based on Synapse user info.
     */
    def isAuthorized(email: String) = {
      // TODO: Cross-check with Synapse for tighter security
      email.toLowerCase().endsWith("@sagebase.org")
    }
  }
}
