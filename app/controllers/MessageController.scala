/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import connectors.{ObjectStoreConnector, UpscanConnector}
import models.formats.HttpFormats
import models.upscan.CreateMovementResponse
import models.values.{MessageId, MovementId, UpscanReference}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Json.toJson
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.objectstore.client.{Object => Thingy}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.nio.file.Path
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MessageController @Inject() (
  upscanConnector: UpscanConnector,
  objectStoreConnector: ObjectStoreConnector,
  cc: ControllerComponents
)(implicit mat: Materializer, ec: ExecutionContext)
  extends BackendController(cc)
  with HttpFormats
  with Logging
  with I18nSupport
  with StreamingBodyParser {

  // .../movements/[arrivals|departures]
  // Upload initiated by 3rd party app
  // in the main api, this will consider the request a large message init and then
  // - call the initiate endpoint on upscan
  // - create a movement object in mongo
  // - pass back the upscan upload details and the movement id to the 3rd party consumer
  def createMovement() = Action.async { implicit request =>
    implicit val hc = HeaderCarrierConverter.fromRequest(request)
    val movementId  = MovementId.next()
    upscanConnector.initiate(movementId).map {
      case Left(error: UpstreamErrorResponse) =>
        BadRequest(error.message)

      case Right(result) =>
        // TODO Save metadata to database
        Created(toJson(CreateMovementResponse(movementId, result.uploadRequest)))
    }
  }

  // .../movements/[arrivals|departures]/{movementId}/messages
  // creates a new message record
  // called by upscan callback (and the 3rd party app to send a message from the trader, and the eis router to send a message from ERMIS)
  // no need to store object-store reference since the path is made up from movement and message ids
  def create(movementId: MovementId) = Action.async(parse.json) { implicit request =>
    implicit val hc = HeaderCarrierConverter.fromRequest(request)
    val reference   = request.body \ "reference"

    print(s"reference $reference")

    // 1. Download the file to a local temporary file
    upscanConnector
      .downloadToFile(UpscanReference(reference.as[String]))
      .flatMap(_ match {
        case Right(path: Path) => {
          // TODO Create new message record
          val messageId = MessageId.next()
          // TODO extract meta data, inc file type
          // TODO validate file against XSD
          // TODO set message sender
          // TODO set message record as Pending
          // 2. store the file in object-store
          objectStoreConnector
            .upload(movementId, messageId, path)
            .flatMap(_ match {
              case Right(_) => {
                // 3. forward on the file to SDES
                // TODO forward on the file to SDES
                Future.successful(Created)
              }
              case Left(_) => {
                Future.successful(InternalServerError)
                // TODO store the error in the message record and status = Failed?
                // TODO push a failure message out to PPNS?
              }
            })
        }
        case Left(_) => {
          // TODO store the error in the message record and status = Failed?
          // TODO push a failure message out to PPNS?
          Future.successful(InternalServerError)
        }
      })
  }

  // .../movements/[arrivals|departures]/messages/{messageId}
  // returns the whole message from either the mongodb (small messages) or object-store
  def get(movementId: MovementId, messageId: MessageId) = Action.async(parse.stream) { request =>
    implicit val hc = HeaderCarrierConverter.fromRequest(request)
    // Assuming that we're not returning small messages in this PoC
    // We have to return the whole message from object-store, not an external reference (s3 url) because object-store does not support external references
    objectStoreConnector
      .get(movementId, messageId)
      .flatMap(_ match {
        case Right(source: Thingy[Source[ByteString, _]]) =>
          Future.successful(
            Ok.streamed(
              source.content,
              contentLength = Some(source.metadata.contentLength),
              contentType = Some(source.metadata.contentType)
            ).withHeaders("Content-MD5" -> source.metadata.contentMd5.value)
          )
        case Left(_) => Future.successful(InternalServerError)
      })

  }

  // callback from SDES when the file has been processed
  // /rpc/sdes/callback
  def sdessuccess(movementId: MovementId, messageId: MessageId) = Action.async(parse.json) {
    request =>
      // TODO update movement-message record to set status = submitted
      Future.successful(Ok)
  }
}
