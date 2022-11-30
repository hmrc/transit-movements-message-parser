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

package services

import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import io.lemonlabs.uri.AbsoluteUrl
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.http.UpstreamErrorResponse

import java.nio.file.{Files, Path}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class MessageDownloadService @Inject() (
  wsClient: WSClient
)(implicit mat: Materializer, ec: ExecutionContext) {

  private def isFailureStatus(status: Int) =
    status / 100 >= 4

  def downloadFile(
    fileUrl: AbsoluteUrl
  ): Future[Either[UpstreamErrorResponse, Source[ByteString, _]]] = {
    wsClient.url(fileUrl.toString).get().map { res =>
      if (isFailureStatus(res.status))
        Left(UpstreamErrorResponse(res.body, res.status, res.status, res.headers))
      else
        Right(res.bodyAsSource)
    }
  }

  def downloadToTemporaryFile(fileUrl: AbsoluteUrl): Future[Either[UpstreamErrorResponse, Path]] = {
    val futureResponse: Future[WSResponse] =
      wsClient.url(fileUrl.toString()).withMethod("GET").stream()

    val file = Files.createTempFile("message-", "xml")
    futureResponse.flatMap { res =>
      res.bodyAsSource
        .runWith(FileIO.toPath(file))
        .map(_ => Right(file))
        .recover { case NonFatal(ex) =>
          Left(UpstreamErrorResponse(ex.getMessage, INTERNAL_SERVER_ERROR))
        }
    }
  }

}
