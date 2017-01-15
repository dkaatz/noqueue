package controllers

import javax.inject.Inject
import javax.security.auth.login.CredentialException

import api.ApiError
import api.JsonCombinators._
import api.auth.Credentials
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by anwender on 09.01.2017.
 */

class WarteschlangenPlatz @Inject() (val messagesApi: MessagesApi, val config: Configuration) extends api.ApiController {

  val dlUndMitarbeiterReads = (
    (__ \ "dienstleistung").read[Long] and
    (__ \ "mitarbeiter").read[Long]
  )((dlId, mitarbeiterId) => (dlId, mitarbeiterId))

  def create = SecuredApiActionWithBody { implicit request =>
    readFromRequest[(Long, Long)] {
      case (dlId, mitarbeiterId) =>
        val wsP = request.anwender.wsFuerBestimmtenMitarbeiterBeitreten(dlId, mitarbeiterId)
        okF(wsP)
    }(request, dlUndMitarbeiterReads, request.request) //request and req.req are the vals that would have also been taken if they hadn't been declared
  }
}