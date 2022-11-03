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
import connectors.{ObjectStoreConnector, UpscanConnector}
import generators.ModelGenerators
import models.upscan.{UpscanInitiateResponse, UpscanNotification}
import models.values.{MessageId, MovementId, UpscanReference}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import java.nio.file.{Files, Path}
import scala.concurrent.Future

class MessageControllerSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  "MessageController" - {

    "initiateUpload" - {

      "must return Created when upscan initiate response returned from connector" in {
        val mockUpscanConnector: UpscanConnector = mock[UpscanConnector]

        val application = baseApplicationBuilder
          .overrides(
            bind[UpscanConnector].toInstance(mockUpscanConnector)
          )
          .build()

        running(application) {

          forAll(arbitrary[UpscanInitiateResponse]) { (response) =>
            reset(mockUpscanConnector)

            when(mockUpscanConnector.initiate(any[MovementId])(any[HeaderCarrier]))
              .thenReturn(Future.successful(Right(response)))

            val request =
              FakeRequest(POST, routes.MessageController.createMovement().url)

            val result = route(application, request).value

            status(result) mustEqual CREATED

            verify(mockUpscanConnector).initiate(any[MovementId])(any[HeaderCarrier])

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

          forAll(arbitrary[UpstreamErrorResponse]) { (response) =>
            reset(mockUpscanConnector)

            when(mockUpscanConnector.initiate(any[MovementId])(any[HeaderCarrier]))
              .thenReturn(Future.successful(Left(response)))

            val request =
              FakeRequest(POST, routes.MessageController.createMovement().url)

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST

            verify(mockUpscanConnector).initiate(any[MovementId])(any[HeaderCarrier])

            contentAsString(result) mustEqual response.message
          }
        }
      }
    }

    "onScanComplete" - {
      "must return Created" in {
        val mockUpscanConnector: UpscanConnector           = mock[UpscanConnector]
        val mockObjectStoreConnector: ObjectStoreConnector = mock[ObjectStoreConnector]

        val application = baseApplicationBuilder
          .overrides(
            bind[UpscanConnector].toInstance(mockUpscanConnector),
            bind[ObjectStoreConnector].toInstance(mockObjectStoreConnector)
          )
          .build()

        running(application) {

          forAll(
            arbitrary[MovementId],
            arbitrary[UpscanNotification]
          ) { (messageId, upscanNotification) =>
            val path = Files.createTempFile("test", ".xml")

            reset(mockUpscanConnector)
            when(mockUpscanConnector.downloadToFile(any[UpscanReference]))
              .thenReturn(Future.successful(Right(path)))

            reset(mockObjectStoreConnector)
            when(mockObjectStoreConnector.upload(any[MovementId], any[MessageId], any[Path]))
              .thenReturn(Future.successful(Right(None)))

            val request =
              FakeRequest(POST, routes.MessageController.create(messageId).url)
                .withJsonBody(Json.toJson(upscanNotification))

            val result = route(application, request).value

            status(result) mustEqual CREATED
          }
        }
      }
    }
  }
}
