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

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString
import base.{FailureWSResponse, SpecBase, SuccessWSResponse}
import generators.ModelGenerators
import io.lemonlabs.uri.AbsoluteUrl
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.Helpers.running
import uk.gov.hmrc.http.UpstreamErrorResponse

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class MessageDownloadServiceSpec
  extends SpecBase
  with ScalaCheckPropertyChecks
  with ModelGenerators {

  "MessageDownloadService" - {
    "downloadFile" - {
      "must return body as source for 2xx" in {
        val mockWSClient  = mock[WSClient]
        val mockWSRequest = mock[WSRequest]

        val application = baseApplicationBuilder
          .overrides(
            bind[WSClient].toInstance(mockWSClient)
          )
          .build()

        running(application) {

          forAll(
            Gen.choose(200, 299),
            arbitrary[AbsoluteUrl],
            arbitrary[Source[ByteString, NotUsed]]
          ) { (statusCode, url, source) =>
            reset(mockWSClient, mockWSRequest)

            when(mockWSClient.url(any[String])).thenReturn(mockWSRequest)

            when(mockWSRequest.get())
              .thenReturn(Future.successful(new SuccessWSResponse(statusCode, source)))

            val service = application.injector.instanceOf[MessageDownloadService]

            val result = Await.result(service.downloadFile(url), Duration.Inf)

            result.right.get mustBe source

            verify(mockWSClient).url(eqTo(url.toString))
            verify(mockWSRequest).get()
          }
        }
      }

      "must return upstream error response for 4xx/5xx" in {
        val mockWSClient  = mock[WSClient]
        val mockWSRequest = mock[WSRequest]

        val application = baseApplicationBuilder
          .overrides(
            bind[WSClient].toInstance(mockWSClient)
          )
          .build()

        running(application) {

          forAll(
            Gen.choose(400, 599),
            arbitrary[AbsoluteUrl],
            arbitrary[String],
            arbitrary[Map[String, Seq[String]]]
          ) { (statusCode, url, body, headers) =>
            reset(mockWSClient, mockWSRequest)

            when(mockWSClient.url(any[String])).thenReturn(mockWSRequest)

            when(mockWSRequest.get())
              .thenReturn(Future.successful(new FailureWSResponse(statusCode, body, headers)))

            val service = application.injector.instanceOf[MessageDownloadService]

            val result = Await.result(service.downloadFile(url), Duration.Inf)

            result.left.get mustBe UpstreamErrorResponse(body, statusCode, statusCode, headers)

            verify(mockWSClient).url(eqTo(url.toString))
            verify(mockWSRequest).get()
          }
        }
      }
    }
  }
}
