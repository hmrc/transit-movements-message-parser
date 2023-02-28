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
import io.lemonlabs.uri.{AbsoluteUrl, UrlPath}
import models.formats.HttpFormats
import models.upscan.{UpscanInitiateRequest, UpscanInitiateResponse}
import models.values.{MovementId, UpscanReference}
import services.MessageDownloadService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpscanConnector @Inject() (
  http: HttpClient,
  appConfig: AppConfig,
  downloadService: MessageDownloadService
)(implicit
  ec: ExecutionContext
) extends HttpFormats {

  private val initiateUrl: AbsoluteUrl =
    appConfig.upscanInitiateUrl.withPath(UrlPath(Seq("upscan", "v2", "initiate")))
  private val upscanUrl: AbsoluteUrl = appConfig.upscanUrl

  def initiate(movementId: MovementId)(implicit
    hc: HeaderCarrier
  ): Future[Either[UpstreamErrorResponse, UpscanInitiateResponse]] = {
    val scanCompletePath =
      UrlPath.parse(controllers.routes.MessageController.create(movementId).path())

    val initiateRequest = UpscanInitiateRequest(
      callbackUrl = appConfig.selfUrl.withPath(scanCompletePath),
      minimumFileSize = appConfig.upscanMinimumFileSize,
      maximumFileSize = appConfig.upscanMaximumFileSize
    )

    println("initiate begin")

    val res =
      http.POST[UpscanInitiateRequest, Either[UpstreamErrorResponse, UpscanInitiateResponse]](
        initiateUrl.toString,
        initiateRequest
      )

    println("initiate complete")

    res
  }

  def downloadToFile(
    downloadUrl: AbsoluteUrl
  ) = {
    downloadService.downloadToTemporaryFile(downloadUrl)
  }

}
