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
import models.upscan.{UpscanInitiateRequest, UpscanInitiateResponse}
import models.values.MessageId
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpscanConnector @Inject() (http: HttpClient, appConfig: AppConfig)(implicit
  ec: ExecutionContext
) extends HttpFormats {

  private val initiateUrl: AbsoluteUrl =
    appConfig.upscanInitiateUrl.withPath(UrlPath(Seq("upscan", "v2", "initiate")))

  def initiate(messageId: MessageId)(implicit
    hc: HeaderCarrier
  ): Future[Either[UpstreamErrorResponse, UpscanInitiateResponse]] = {
    val scanCompletePath =
      UrlPath.parse(controllers.routes.MessageController.onScanComplete(messageId).path())
    val uploadSuccessPath =
      UrlPath.parse(controllers.routes.MessageController.onUploadSuccess(messageId).path())
    val uploadFailurePath =
      UrlPath.parse(controllers.routes.MessageController.onUploadFailure(messageId).path())

    val initiateRequest = UpscanInitiateRequest(
      callbackUrl = appConfig.selfUrl.withPath(scanCompletePath),
      successRedirect = appConfig.selfUrl.withPath(uploadSuccessPath),
      errorRedirect = appConfig.selfUrl.withPath(uploadFailurePath),
      minimumFileSize = appConfig.upscanMinimumFileSize,
      maximumFileSize = appConfig.upscanMaximumFileSize
    )

    http.POST[UpscanInitiateRequest, Either[UpstreamErrorResponse, UpscanInitiateResponse]](
      initiateUrl.toString,
      initiateRequest
    )
  }

}
