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
import io.lemonlabs.uri.{AbsoluteUrl, UrlPath}
import models.formats.HttpFormats
import models.sdes.{SdesFilereadyRequest, SdesFilereadyResponse}
import models.upscan.{UpscanInitiateRequest, UpscanInitiateResponse}
import models.values.{MessageId, MovementId, UpscanReference}
import services.MessageDownloadService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SdesConnector @Inject()(
  http: HttpClient,
  appConfig: AppConfig
)(implicit
  ec: ExecutionContext
) extends HttpFormats {

  private val sdesUrl: String = appConfig.sdesUrl
  private val sdesUri: String = appConfig.sdesFilereadyUri

  def send(movementId: MovementId, messageId: MessageId)(implicit
    hc: HeaderCarrier
  ): Future[Either[UpstreamErrorResponse, UpscanInitiateResponse]] = {
    val request = SdesFilereadyRequest(
      UUID.randomUUID.toString,
      ObjectStoreConnector.pathFrom(movementId, messageId)
    )

    http.POST[SdesFilereadyRequest, Either[UpstreamErrorResponse, SdesFilereadyResponse]](
      sdesUrl + sdesUri,
      request
    )
  }

}
