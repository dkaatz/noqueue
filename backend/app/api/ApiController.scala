package api

import api.ApiError._
import api.Api.Sorting._
import api.jwt.{ JwtSecret, JwtUtil, TokenPayload }
import org.joda.time.DateTime
import play.api.Configuration
import play.api.mvc._
import javax.inject.Inject

import models.PostgresDB
import models.db.DAL

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.i18n.{ I18nSupport, MessagesApi }

import scala.util.{ Failure, Success, Try }
import play.api.libs.json._
import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcBackend.Database

/**
 * Controller trait for API controllers
 */
trait ApiController extends Controller with I18nSupport {
  val db: Database = PostgresDB.db;
  val dal = PostgresDB.dal;
  val config: Configuration;
  val messagesApi: MessagesApi
  implicit protected val SECRET: JwtSecret = JwtSecret(config.getString("jwt.token.secret").get);
  ////////////////////////////////////
  /// Transformation Uitlities
  ////////////////////////////////////

  /**
   * Transforms an Object to JSON
   *
   * @param o Generic Object
   * @param tjs JsWriter
   * @tparam T Type of Generic Object
   * @return
   */
  implicit def objectToJson[T](o: T)(implicit tjs: Writes[T]): JsValue = Json.toJson(o)

  /**
   * Transforms a result to a future
   *
   * @param r Result Object
   * @return
   */
  implicit def result2FutureResult(r: ApiResult): Future[ApiResult] = Future.successful(r)

  //////////////////////////////////////////////////////////////////////
  // Custom Actions
  //////////////////////////////////////////////////////////////////////

  /**
   * Simple API-Action
   *
   * @param action callback action
   * @return
   */
  def ApiAction(action: ApiRequest[Unit] => Future[ApiResult]) = ApiActionWithParser(parse.empty)(action)

  /**
   * Simple API-Action that requires a Body
   *
   * @param action callback action
   * @return
   */
  def ApiActionWithBody(action: ApiRequest[JsValue] => Future[ApiResult]) = ApiActionWithParser(parse.json)(action)

  /**
   * Api Action that requires authentication
   * @param action callback action
   * @return
   */
  def SecuredApiAction(action: SecuredApiRequest[Unit] => Future[ApiResult]) = SecuredApiActionWithParser(parse.empty)(action)

  /**
   * Api Action that requires authentication and a Body
   *
   * @param action callback action
   * @return
   */
  def SecuredApiActionWithBody(action: SecuredApiRequest[JsValue] => Future[ApiResult]) = SecuredApiActionWithParser(parse.json)(action)

  /**
   * Creates an Action checking that the Request has all the common necessary headers with their correct values
   *
   * @param parser body parser to parse request body
   * @param action callback action
   * @tparam A type of body data
   * @return
   */
  private def ApiActionCommon[A](parser: BodyParser[A])(action: (ApiRequest[A]) => Future[ApiResult]) = Action.async(parser) { implicit request =>

    implicit val apiRequest = ApiRequest(request)

    val futureApiResult: Future[ApiResult] = action(apiRequest)

    futureApiResult.map {
      case error: ApiError => error.saveLog.toResult
      case response: ApiResponse => response.toResult
    }

  }

  /**
   * Basic API action wich requries (actually) no additional headers
   *
   * @param parser bodyparser that parses the request body
   * @param action callback action
   * @tparam A Type of body data
   * @return
   */
  private def ApiActionWithParser[A](parser: BodyParser[A])(action: ApiRequest[A] => Future[ApiResult]) = ApiActionCommon(parser) { (apiRequest) =>
    action(apiRequest)
  }

  /**
   * Secured API action that verifies a
   *
   * @param parser bodyparser that parses the request body
   * @param action callback action
   * @tparam A Type of body data
   * @return
   */
  private def SecuredApiActionWithParser[A](parser: BodyParser[A])(action: SecuredApiRequest[A] => Future[ApiResult]) = ApiActionCommon(parser) { (apiRequest) =>
    apiRequest.tokenOpt match {
      case None => errorTokenNotFound
      case Some(token) => JwtUtil.getPayloadIfValidToken[TokenPayload](token).flatMap {
        case None => errorTokenUnknown
        case Some(payload) => action(SecuredApiRequest(apiRequest.request, payload))
      }
    }
  }

  //////////////////////////////////////////////////////////////////////
  // Auxiliar methods to create ApiResults from writable JSON objects

  /**
   * Auxiliar method that creates an API Result
   *
   * @param obj writable json object
   * @param headers headers to be send
   * @param w json serialization object
   * @tparam A type of write object
   */
  def ok[A](obj: A, headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult] = Future.successful(ApiResponse.ok(obj, headers: _*))

  /**
   *
   * @param futObj future object
   * @param headers headers to be send
   * @param w json serialization object
   * @tparam A type to write
   * @return
   */
  def ok[A](futObj: Future[A], headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult] = futObj.map(obj => ApiResponse.ok(obj, headers: _*))

  /**
   * Returns api response with item or an error, this helper is usefull to send out on searches when we are not sure if any
   * result will be returned
   *
   * @param opt optiona of type A
   * @param headers headers to bes end
   * @param w json serialization object
   * @param req request header
   * @tparam A type of object to be serialized
   * @return
   */
  private def itemOrError[A](opt: Option[A], headers: (String, String)*)(implicit w: Writes[A], req: RequestHeader): ApiResult = opt match {
    case Some(i) => ApiResponse.ok(i, headers: _*)
    case None => ApiError.errorItemNotFound
  }

  /**
   * Returns future with item or an error, this helper is usefull to send out on searches when we are not sure if any
   * result will be returned
   *
   * @param opt optional object
   * @param headers headers to be send
   * @param w json serialization object
   * @param req request headers
   * @tparam A type of optional object to be serialized
   * @return
   */
  def maybeItem[A](opt: Option[A], headers: (String, String)*)(implicit w: Writes[A], req: RequestHeader): Future[ApiResult] = Future.successful(itemOrError(opt, headers: _*))

  /**
   * Returns future with item or an error, this helper is usefull to send out on searches when we are not sure if any
   * result will be returned
   *
   * @param futOpt future object
   * @param headers headerts to be send
   * @param w json serialization object
   * @param req request headers
   * @tparam A type of future object to be serialized
   * @return
   */
  def maybeItem[A](futOpt: Future[Option[A]], headers: (String, String)*)(implicit w: Writes[A], req: RequestHeader): Future[ApiResult] = futOpt.map(opt => itemOrError(opt, headers: _*))

  /**
   * Pagination helper to send back paged results
   *
   * @param p page of object
   * @param headers heders to be send
   * @param w json serialization object
   * @tparam A type of page object
   * @return
   */
  def page[A](p: Page[A], headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult] = Future.successful(ApiResponse.ok(p.items, p, headers: _*))

  /**
   * Pagination helper to send back paged results
   *
   * @param futP future page object
   * @param headers heders to be send
   * @param w json serialization object
   * @tparam A type of page object
   * @return
   */
  def page[A](futP: Future[Page[A]], headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult] = futP.map(p => ApiResponse.ok(p.items, p, headers: _*))

  /**
   * Helper to handle sorted page results
   *
   * @param sortBy the key to sort for
   * @param allowedFields the allowed fields
   * @param default the default sort
   * @param name name of sort (optional)
   * @param headers headers to be send
   * @param p page object
   * @param w json serialization object
   * @param req request headers
   * @tparam A type of response object
   * @return
   */
  def sortedPage[A](
    sortBy: Option[String],
    allowedFields: Seq[String],
    default: String,
    name: String = "sort",
    headers: Seq[(String, String)] = Seq()
  )(p: Seq[(String, Boolean)] => Future[Page[A]])(implicit w: Writes[A], req: RequestHeader): Future[ApiResult] = {
    processSortByParam(sortBy, allowedFields, default, name).fold(
      error => error,
      sortFields => page(p(sortFields), headers: _*)
    )
  }

  /**
   *
   *
   * @param obj response object
   * @param headers headers to be send
   * @param w json serialization object
   * @tparam A type of response object
   * @return
   */
  def created[A](obj: A, headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult] = Future.successful(ApiResponse.created(obj, headers: _*))

  /**
   *
   * @param futObj future response object
   * @param headers headers to be send
   * @param w json serialization object
   * @tparam A type of response object
   * @return
   */
  def created[A](futObj: Future[A], headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult] = futObj.map(obj => ApiResponse.created(obj, headers: _*))

  /**
   * @param headers headers to be send
   * @return
   */
  def created(headers: (String, String)*): Future[ApiResult] = Future.successful(ApiResponse.created(headers: _*))

  /**
   *
   * @param obj response object
   * @param headers headers to be send
   * @param w json serialization object
   * @tparam A type of response object
   * @return
   */
  def accepted[A](obj: A, headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult] = Future.successful(ApiResponse.accepted(obj, headers: _*))

  /**
   *
   * @param futObj future response object
   * @param headers headers to be send
   * @param w json serialization object
   * @tparam A type of future response object
   * @return
   */
  def accepted[A](futObj: Future[A], headers: (String, String)*)(implicit w: Writes[A]): Future[ApiResult] = futObj.map(obj => ApiResponse.accepted(obj, headers: _*))

  /**
   *
   * @param headers headers to be send
   * @return
   */
  def accepted(headers: (String, String)*): Future[ApiResult] = Future.successful(ApiResponse.accepted(headers: _*))

  /**
   *
   * @param headers headers to be send
   * @return
   */
  def noContent(headers: (String, String)*): Future[ApiResult] = Future.successful(ApiResponse.noContent(headers: _*))

  //////////////////////////////////////////////////////////////////////
  // More auxiliar methods

  /**
   * Reads an object from an ApiRequest[JsValue] handling a possible malformed error
   *
   * @param f future api result
   * @param request Api Request
   * @param rds json deserializer
   * @param req request headers
   * @tparam T type of api result object
   * @return
   */
  def readFromRequest[T](f: T => Future[ApiResult])(implicit request: ApiRequest[JsValue], rds: Reads[T], req: RequestHeader): Future[ApiResult] = {
    request.body.validate[T].fold(
      errors => errorBodyMalformed(errors),
      readValue => f(readValue)
    )
  }

  /**
   * Process a "sort" URL GET param with a specific format. Returns the corresponding description as a list of pairs field-order,
   * where field is the field to sort by, and order indicates if the sorting has an ascendent or descendent order.
   * The input format is a string with a list of sorting fields separated by commas and with preference order. Each field has a
   * sign that indicates if the sorting has an ascendent or descendent order.
   * Example: "-done,order,+id"  Seq(("done", DESC), ("priority", ASC), ("id", ASC))   where ASC=false and DESC=true
   *
   * @param sortBy optional String with the input sorting description.
   * @param allowedFields a list of available allowed fields to sort.
   * @param default String with the default input sorting description.
   * @param name the name of the param.
   * @param req request header
   * @return
   */
  def processSortByParam(sortBy: Option[String], allowedFields: Seq[String], default: String, name: String = "sort")(implicit req: RequestHeader): Either[ApiError, Seq[(String, Boolean)]] = {
    val signedFieldPattern = """([+-]?)(\w+)""".r
    val fieldsWithOrder = signedFieldPattern.findAllIn(sortBy.getOrElse(default)).toList.map {
      case signedFieldPattern("-", field) => (field, DESC)
      case signedFieldPattern(_, field) => (field, ASC)
    }
    // Checks if every field is within the available allowed ones
    if ((fieldsWithOrder.map(_._1) diff allowedFields).isEmpty)
      Right(fieldsWithOrder)
    else
      Left(errorParamMalformed(name))
  }

}