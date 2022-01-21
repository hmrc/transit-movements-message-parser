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
import connectors.UpscanConnector
import models.MessageType
import models.formats.HttpFormats
import models.upscan.UpscanNotification
import models.values.MessageId
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import models.upscan.UpscanSuccessNotification
import models.upscan.UpscanFailureNotification

class MessageController @Inject() (
  upscanConnector: UpscanConnector,
  cc: ControllerComponents
)(implicit mat: Materializer)
  extends BackendController(cc)
  with HttpFormats
  with Logging
  with I18nSupport
  with StreamingBodyParser {

  implicit val ec: ExecutionContext = mat.executionContext

  // Upload initiated by user
  // Called by CTC Traders Message Upload API
  def initiateUpload(messageType: MessageType) = Action.async { implicit request =>
    // TODO:
    // * Save metadata to database
    upscanConnector.initiate(MessageId.next()).map {
      case Left(value) =>
        BadRequest(value.message)

      case Right(result) =>

        pprint.pprintln(result)

        Ok(Json.toJson(result.uploadRequest))
    }
  }

  // User upload POST request to S3 successful
  // User is redirected to an endpoint in the Message Upload API on success that calls this
  def onUploadSuccess(messageId: MessageId) = Action.async { implicit request =>
    // TODO:
    // * Record upload outcome to DB
    Future.successful(Ok)
  }

  // User upload POST request to S3 failed
  // User is redirected to an endpoint in the Message Upload API on failure that calls this
  def onUploadFailure(messageId: MessageId) = Action.async { implicit request =>
    // TODO:
    // * Record upload outcome to DB - errors in query parameters
    Future.successful(BadRequest)
  }

  // Upscan POST notification after file scanning complete
  // This endpoint is called directly by Upscan on scan completion - must not be accessible externally
  def onScanComplete(messageId: MessageId) = Action.async(parse.json[UpscanNotification]) {
    request =>
      // TODO:
      // If successful:
      // * Download from Upscan bucket to temporary file
      // * Fetch previously-stored metadata
      // * Perform XSD validation
      // * Parse message metadata from XML
      // * Upload to object-store?
      // * Record any XSD / parsing failures to DB
      // If unsuccessful:
      // * Record failed scan result to DB

      pprint.pprintln(request.body)

      Future.successful(Ok)
  }

  // Message body POST from small messages route
  // Called on message POST by CTC Traders API which awaits processing result synchronously
  def receiveMessage = Action.async(parse.stream) { request =>
    // TODO:
    // * Perform XSD validation
    // * Parse message metadata from XML
    // * Upload to object-store
    ???
  }
}
