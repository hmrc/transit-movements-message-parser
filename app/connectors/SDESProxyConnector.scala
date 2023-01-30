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

package connectors

import config.AppConfig
import models.formats.HttpFormats
import models.sdes._
import models.values.ConversationId
import models.values.MessageId
import models.values.MovementId
import play.api.Logging
import play.api.http.HeaderNames
import play.api.http.MimeTypes
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.StringContextOps
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.objectstore.client.ObjectSummaryWithMd5

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class SDESProxyConnector @Inject() (
  httpClientV2: HttpClientV2,
  appConfig: AppConfig
)(implicit
  ec: ExecutionContext
) extends HttpFormats
  with Logging {

  private lazy val sdesFileReadyUrl =
    appConfig.sdesProxyUrl.withPath(appConfig.sdesProxyFileReadyUri)
  private lazy val clientId = appConfig.sdesProxyClientId
  private lazy val srn      = appConfig.sdesProxySrn

  def send(movementId: MovementId, messageId: MessageId, objectStoreSummary: ObjectSummaryWithMd5)(
    implicit hc: HeaderCarrier
  ): Future[Either[UpstreamErrorResponse, Unit]] = {
    val request = SdesFilereadyRequest(
      appConfig.sdesProxyInformationType,
      file = SdesFile(
        srn,
        objectStoreSummary.location.fileName,
        objectStoreSummary.location.directory.asUri,
        SdesChecksum(objectStoreSummary.contentMd5.value),
        objectStoreSummary.contentLength,
        Seq(
          SdesProperties("x-conversation-id", ConversationId(movementId, messageId).value.toString)
        )
      ),
      SdesAudit(UUID.randomUUID.toString)
    )

    logger.info(s"sdes")
    println(s"sdes")

    httpClientV2
      .post(url"$sdesFileReadyUrl")
      .setHeader("X-Client-Id" -> clientId)
      .setHeader(HeaderNames.CONTENT_TYPE -> MimeTypes.JSON)
      .withBody(Json.toJson(request))
      .execute[Either[UpstreamErrorResponse, Unit]]
      .map {
        case Left(error) =>
          logger.info(s"CTC to SDES error message: ${error.message} - ${error.statusCode}");
          Left(error)
        case Right(()) => logger.info(s"CTC to SDES successful"); Right(())
      }
  }

}
