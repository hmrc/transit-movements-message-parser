/*
 * Copyright 2023 HM Revenue & Customs
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

package models.sdes

import models.formats.HttpFormats
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.Instant

case class SdesNotificationItem(
  notification: SdesNotification,
  filename: String,
  correlationId: String,
  checksumAlgorithm: String,
  checksum: String,
  availableUntil: Instant,
  failureReason: Option[String],
  dateTime: Instant,
  properties: Seq[SdesProperties]
)

object SdesNotificationItem extends HttpFormats {

  implicit val reads: Reads[SdesNotificationItem] =
    ((__ \ "notification").read[SdesNotification]((json: JsValue) =>
      JsSuccess(SdesNotification.parse(json.as[JsString].value).get)
    ) and
      (__ \ "filename").read[String] and
      (__ \ "correlationID").read[String] and
      (__ \ "checksumAlgorithm").read[String] and
      (__ \ "checksum").read[String] and
      (__ \ "availableUntil").read[Instant] and
      (__ \ "failureReason").readNullable[String] and
      (__ \ "dateTime").read[Instant] and
      (__ \ "properties").read[Seq[SdesProperties]])(SdesNotificationItem.apply _)
}
