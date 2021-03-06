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

package models.formats

import io.lemonlabs.uri.AbsoluteUrl
import io.lemonlabs.uri.Uri
import models.upscan._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.objectstore.client.Md5Hash

import java.net.URI
import models.upscan.UpscanInitiateResponse

trait HttpFormats extends CommonFormats {
  implicit val absoluteUrlFormat: Format[AbsoluteUrl] = Format
    .of[URI]
    .inmap[AbsoluteUrl](
      Uri(_).toUrl.toAbsoluteUrl,
      _.toJavaURI
    )

  implicit val md5HashFormat: Format[Md5Hash] = Format
    .of[String]
    .inmap(Md5Hash.apply, _.value)

  implicit val upscanFileStatusFormat: Format[UpscanFileStatus] =
    enumFormat(UpscanFileStatus.values)(_.name)

  implicit val upscanFailureReasonFormat: Format[UpscanFailureReason] =
    enumFormat(UpscanFailureReason.values)(_.name)

  implicit val upscanUploadDetailsFormat: OFormat[UpscanUploadDetails] =
    Json.format[UpscanUploadDetails]
  implicit val upscanFailureDetailsFormat: OFormat[UpscanFailureDetails] =
    Json.format[UpscanFailureDetails]

  implicit val upscanSuccessNotificationFormat: OFormat[UpscanSuccessNotification] =
    Json.format[UpscanSuccessNotification]
  implicit val upscanFailureNotificationFormat: OFormat[UpscanFailureNotification] =
    Json.format[UpscanFailureNotification]

  implicit val upscanNotificationReads: Reads[UpscanNotification] =
    (__ \ "fileStatus").read[UpscanFileStatus].flatMap {
      case Ready  => upscanSuccessNotificationFormat.widen[UpscanNotification]
      case Failed => upscanFailureNotificationFormat.widen[UpscanNotification]
    }

  implicit val upscanNotificationFormat: OFormat[UpscanNotification] =
    OFormat(
      upscanNotificationReads,
      Json.writes[UpscanNotification]
    )

  implicit val upscanInitiateRequestFormat: OFormat[UpscanInitiateRequest] =
    Json.format[UpscanInitiateRequest]

  implicit val upscanFormTemplateFormat: OFormat[UpscanFormTemplate] =
    Json.format[UpscanFormTemplate]

  implicit val upscanInitiateResponseFormat: OFormat[UpscanInitiateResponse] =
    Json.format[UpscanInitiateResponse]
}
