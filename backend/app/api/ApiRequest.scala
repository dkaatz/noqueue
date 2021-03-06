package api

import Api._
import play.api.mvc._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Locale

import api.jwt.TokenPayload
import com.nimbusds.jose.Payload
import models.{ Anwender, Leiter, Mitarbeiter }

import scala.util.Try
import play.api.libs.json._

import scala.concurrent.Future

/*
* Wrapped Request with additional information for the API
*/
trait ApiRequestHeader[R <: RequestHeader] {
  val request: R

  val tokenOpt: Option[String] = request.headers.get(HEADER_AUTH_TOKEN)

  def now: DateTime = new DateTime()
  def remoteAddress: String = request.remoteAddress
  def method: String = request.method
  def uri: String = request.uri
  def maybeBody: Option[String] = None
}

case class ApiRequestHeaderImpl(val request: RequestHeader) extends ApiRequestHeader[RequestHeader]

/*
* ApiRequestHeader for requests that don't require authentication
*/
class ApiRequest[A](val request: Request[A]) extends WrappedRequest[A](request) with ApiRequestHeader[Request[A]] {
  override def remoteAddress = request.remoteAddress
  override def method = request.method
  override def uri = request.uri
  override def maybeBody: Option[String] = request.body match {
    case body: JsValue => Some(Json.prettyPrint(body))
    case body: String => if (body.length > 0) Some(body) else None
    case body => Some(body.toString)
  }
}

/**
 * Default api request
 */
object ApiRequest {
  def apply[A](request: Request[A]): ApiRequest[A] = new ApiRequest[A](request)
}

/**
 * ApiRequest for authenticated Anwender requests
 *
 * @param request the request object
 * @param anwender the authenticated Anwender(model)
 * @tparam A the type of the request object data
 */
case class SecuredAnwenderApiRequest[A](override val request: Request[A], anwender: Anwender) extends ApiRequest[A](request)

/**
 * ApiRequest for authenticated Leiter requests
 *
 * @param request the request object
 * @param leiter the authenticated Leiter(model)
 * @tparam A the type of the request object data
 */
case class SecuredLeiterApiRequest[A](override val request: Request[A], leiter: Leiter) extends ApiRequest[A](request)

/**
 * ApiRequest for authenticated Mitarbeiter requests
 *
 * @param request the request object
 * @param mitarbeiter the authenticated Mitarbeiter(model)
 * @tparam A the type of the request object data
 */
case class SecuredMitarbeiterApiRequest[A](override val request: Request[A], mitarbeiter: Mitarbeiter) extends ApiRequest[A](request)

case class SecuredApiRequest[A](override val request: Request[A], anwender: Anwender) extends ApiRequest[A](request)
