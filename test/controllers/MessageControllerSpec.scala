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

package controllers

import base.SpecBase
import cats.data.EitherT
import connectors.SDESProxyConnector
import connectors.{ObjectStoreConnector, UpscanConnector}
import generators.ModelGenerators
import models.upscan.{UpscanInitiateResponse, UpscanSuccessNotification}
import models.values.{MessageId, MovementId}
import org.mockito.ArgumentMatchers.anyString
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import org.mockito.ArgumentMatchers.{eq => eqTo}
import uk.gov.hmrc.objectstore.client.Md5Hash
import uk.gov.hmrc.objectstore.client.ObjectSummaryWithMd5
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.Path.Directory

import java.nio.file.Files
import java.time.Instant
import scala.concurrent.Future
import scala.util.control.NonFatal

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

            val movementId = contentAsJson(result) \ "movementId"
            contentAsJson(result) mustEqual Json.obj(
              "movementId"    -> movementId.get,
              "uploadRequest" -> response.uploadRequest
            )
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
        val mockSDESProxyConnector: SDESProxyConnector     = mock[SDESProxyConnector]

        val application = baseApplicationBuilder
          .overrides(
            bind[UpscanConnector].toInstance(mockUpscanConnector),
            bind[ObjectStoreConnector].toInstance(mockObjectStoreConnector),
            bind[SDESProxyConnector].toInstance(mockSDESProxyConnector)
          )
          .build()

        running(application) {

          forAll(
            arbitrary[MovementId],
            arbitrary[UpscanSuccessNotification]
          ) { (movementId, upscanNotification) =>
            val path = Files.createTempFile("test", ".xml")

            reset(mockUpscanConnector)
            when(mockUpscanConnector.downloadToFile(eqTo(upscanNotification.reference)))
              .thenReturn(Future.successful(Right(path)))

            reset(mockObjectStoreConnector)
            when(
              mockObjectStoreConnector.upload(
                eqTo(movementId),
                any[String].asInstanceOf[MessageId],
                eqTo(path)
              )(any[HeaderCarrier])
            )
              .thenReturn(
                Future.successful(
                  Right(
                    ObjectSummaryWithMd5(
                      Path.File(Directory("/"), "test.xml"),
                      200,
                      Md5Hash(""),
                      Instant.now()
                    )
                  )
                )
              )

            reset(mockSDESProxyConnector)
            when(
              mockSDESProxyConnector.send(
                MovementId(anyString()),
                MessageId(anyString()),
                any[ObjectSummaryWithMd5]
              )(any[HeaderCarrier])
            )
              .thenReturn(Future.successful(Right(())))

            val request =
              FakeRequest(POST, routes.MessageController.create(movementId).url)
                .withJsonBody(Json.toJson(upscanNotification))

            val result = route(application, request).value

            status(result) mustEqual CREATED
          }
        }
      }
    }
  }
}
