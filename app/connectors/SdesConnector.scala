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

package connectors

import config.AppConfig
import io.lemonlabs.uri.AbsoluteUrl
import models.formats.HttpFormats
import models.sdes._
import models.values.{MessageId, MovementId}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SdesConnector @Inject() (
  http: HttpClient,
  appConfig: AppConfig
)(implicit
  ec: ExecutionContext
) extends HttpFormats {

  private val sdesUrl = AbsoluteUrl.parse(appConfig.sdesUrl + appConfig.sdesFilereadyUri)

  def send(movementId: MovementId, messageId: MessageId)(implicit
    hc: HeaderCarrier
  ): Future[Either[UpstreamErrorResponse, Unit]] = {
    val request = SdesFilereadyRequest(
      "S18",
      file = SdesFile(
        "ctc_forward",
        "ie015.xml",
        pathFrom(movementId, messageId).toString(),
        SdesChecksum("hhh"),
        200,
        Seq(SdesProperties("movementId", "test"), SdesProperties("messageId", "test2"))
      ),
      SdesAudit(UUID.randomUUID.toString)
    )

    println("sdes")

    http.POST[SdesFilereadyRequest, Either[UpstreamErrorResponse, Unit]](
      sdesUrl.toString(),
      request
    )
  }

  // probably should be in an object-store specific class
  def pathFrom(movementId: MovementId, messageId: MessageId): AbsoluteUrl = {
    val res = AbsoluteUrl.parse(
      s"${appConfig.objectStoreRoot}/movements/${movementId.value}/messages/${messageId.value}"
    )

    println(s"path: $res")

    res
  }

}