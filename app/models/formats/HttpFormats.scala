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

import io.lemonlabs.uri.{AbsoluteUrl, Uri}
import models.sdes._
import models.upscan._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.objectstore.client.Md5Hash

import java.net.URI

trait HttpFormats extends CommonFormats {
  implicit lazy val absoluteUrlFormat: Format[AbsoluteUrl] = Format
    .of[URI]
    .inmap[AbsoluteUrl](
      Uri(_).toUrl.toAbsoluteUrl,
      _.toJavaURI
    )

  implicit lazy val md5HashFormat: Format[Md5Hash] = Format
    .of[String]
    .inmap(Md5Hash.apply, _.value)

  implicit lazy val upscanFileStatusFormat: Format[UpscanFileStatus] =
    enumFormat(UpscanFileStatus.values)(_.name)

  implicit lazy val upscanFailureReasonFormat: Format[UpscanFailureReason] =
    enumFormat(UpscanFailureReason.values)(_.name)

  implicit lazy val upscanUploadDetailsFormat: OFormat[UpscanUploadDetails] =
    Json.format[UpscanUploadDetails]
  implicit lazy val upscanFailureDetailsFormat: OFormat[UpscanFailureDetails] =
    Json.format[UpscanFailureDetails]

  implicit lazy val upscanSuccessNotificationFormat: OFormat[UpscanSuccessNotification] =
    Json.format[UpscanSuccessNotification]
  implicit lazy val upscanFailureNotificationFormat: OFormat[UpscanFailureNotification] =
    Json.format[UpscanFailureNotification]

  implicit lazy val upscanNotificationReads: Reads[UpscanNotification] =
    (__ \ "fileStatus").read[UpscanFileStatus].flatMap {
      case Ready  => upscanSuccessNotificationFormat.widen[UpscanNotification]
      case Failed => upscanFailureNotificationFormat.widen[UpscanNotification]
    }

  implicit lazy val upscanNotificationFormat: OFormat[UpscanNotification] =
    OFormat(
      upscanNotificationReads,
      Json.writes[UpscanNotification]
    )

  implicit lazy val upscanInitiateRequestFormat: OFormat[UpscanInitiateRequest] = {
    Json.format[UpscanInitiateRequest]
  }

  implicit lazy val upscanFormTemplateFormat: OFormat[UpscanFormTemplate] =
    Json.format[UpscanFormTemplate]

  implicit lazy val upscanInitiateResponseFormat: OFormat[UpscanInitiateResponse] =
    Json.format[UpscanInitiateResponse]

  implicit lazy val createMovementResponseFormat: OFormat[CreateMovementResponse] =
    Json.format[CreateMovementResponse]

  implicit lazy val sdesFilereadyRequestFormat: OFormat[SdesFilereadyRequest] =
    Json.format[SdesFilereadyRequest]

  implicit lazy val sdesFileFormat: OFormat[SdesFile] =
    Json.format[SdesFile]

  implicit lazy val sdesAuditFormat: OFormat[SdesAudit] =
    Json.format[SdesAudit]

  implicit lazy val sdesChecksumFormat: OFormat[SdesChecksum] =
    Json.format[SdesChecksum]

  implicit lazy val sdesPropertiesFormat: OFormat[SdesProperties] =
    Json.format[SdesProperties]

  implicit lazy val sdesFilereadyResponseFormat: OFormat[SdesFilereadyResponse] =
    Json.format[SdesFilereadyResponse]
}
