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

package controllers

import base.SpecBase
import connectors.UpscanConnector
import generators.ModelGenerators
import models.RequestMessageType
import models.upscan.{UpscanInitiateResponse, UpscanNotification}
import models.values.MessageId
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.Future

class MessageControllerSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  "MessageController" - {

    "initiateUpload" - {

      "must return Ok when upscan initiate response returned from connector" in {
        val mockUpscanConnector: UpscanConnector = mock[UpscanConnector]

        val application = baseApplicationBuilder
          .overrides(
            bind[UpscanConnector].toInstance(mockUpscanConnector)
          )
          .build()

        running(application) {

          forAll(arbitrary[UpscanInitiateResponse], arbitrary[RequestMessageType]) {
            (response, messageType) =>
              reset(mockUpscanConnector)

              when(mockUpscanConnector.initiate(any[MessageId])(any[HeaderCarrier]))
                .thenReturn(Future.successful(Right(response)))

              val request =
                FakeRequest(GET, routes.MessageController.initiateUpload(messageType).url)

              val result = route(application, request).value

              status(result) mustEqual OK

              verify(mockUpscanConnector).initiate(any[MessageId])(any[HeaderCarrier])

              contentAsJson(result) mustEqual Json.toJson(response.uploadRequest)
          }
        }
      }

      "must return BadRequest when upstream error response returned from connector" in {
        val mockUpscanConnector: UpscanConnector = mock[UpscanConnector]

        val application = baseApplicationBuilder
          .overrides(
            bind[UpscanConnector].toInstance(mockUpscanConnector)
          )
          .build()

        running(application) {

          forAll(arbitrary[UpstreamErrorResponse], arbitrary[RequestMessageType]) {
            (response, requestMessageType) =>
              reset(mockUpscanConnector)

              when(mockUpscanConnector.initiate(any[MessageId])(any[HeaderCarrier]))
                .thenReturn(Future.successful(Left(response)))

              val request =
                FakeRequest(GET, routes.MessageController.initiateUpload(requestMessageType).url)

              val result = route(application, request).value

              status(result) mustEqual BAD_REQUEST

              verify(mockUpscanConnector).initiate(any[MessageId])(any[HeaderCarrier])

              contentAsString(result) mustEqual response.message
          }
        }
      }
    }

    "onUploadSuccess" - {
      "must return Ok" in {
        val application = baseApplicationBuilder
          .build()

        running(application) {

          forAll(arbitrary[MessageId]) { messageId =>
            val request =
              FakeRequest(GET, routes.MessageController.onUploadSuccess(messageId).url)

            val result = route(application, request).value

            status(result) mustEqual OK
          }
        }
      }
    }

    "onUploadFailure" - {
      "must return BadRequest" in {
        val application = baseApplicationBuilder
          .build()

        running(application) {

          forAll(arbitrary[MessageId]) { messageId =>
            val request =
              FakeRequest(GET, routes.MessageController.onUploadFailure(messageId).url)

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
          }
        }
      }
    }

    "onScanComplete" - {
      "must return Ok" in {
        val application = baseApplicationBuilder
          .build()

        running(application) {

          forAll(arbitrary[MessageId], arbitrary[UpscanNotification]) {
            (messageId, upscanNotification) =>
              val request =
                FakeRequest(POST, routes.MessageController.onScanComplete(messageId).url)
                  .withJsonBody(Json.toJson(upscanNotification))

              val result = route(application, request).value

              status(result) mustEqual OK
          }
        }
      }
    }
  }
}
