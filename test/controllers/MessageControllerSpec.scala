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
import io.lemonlabs.uri.AbsoluteUrl
import models.MessageType.DeclarationAmendment
import models.upscan.{UpscanFormTemplate, UpscanInitiateResponse}
import models.values.{MessageId, UpscanReference}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.Future

class MessageControllerSpec extends SpecBase {

  "MessageController" - {

    "initiateUpload" - {

      "must return Ok when upscan initiate response returned from connector" in {
        val mockUpscanConnector: UpscanConnector = mock[UpscanConnector]

        val response = UpscanInitiateResponse(
          UpscanReference("reference"),
          UpscanFormTemplate(AbsoluteUrl.parse(s"$baseUrl/upload-success/some-uuid"), Map())
        )

        when(mockUpscanConnector.initiate(any[MessageId])(any[HeaderCarrier])).thenReturn(Future.successful(Right(response)))

        val application = baseApplicationBuilder
          .overrides(
            bind[UpscanConnector].toInstance(mockUpscanConnector)
          )
          .build()

        running(application) {

          val request = FakeRequest(GET, routes.MessageController.initiateUpload(DeclarationAmendment).url)

          val result = route(application, request).value

          status(result) mustEqual OK

          verify(mockUpscanConnector).initiate(any[MessageId])(any[HeaderCarrier])

          contentAsJson(result) mustEqual Json.toJson(response.uploadRequest)
        }
      }

      "must return BadRequest when upstream error response returned from connector" in {
        val mockUpscanConnector: UpscanConnector = mock[UpscanConnector]

        val message = "Some error message"
        val response = UpstreamErrorResponse(message, INTERNAL_SERVER_ERROR)

        when(mockUpscanConnector.initiate(any[MessageId])(any[HeaderCarrier])).thenReturn(Future.successful(Left(response)))

        val application = baseApplicationBuilder
          .overrides(
            bind[UpscanConnector].toInstance(mockUpscanConnector)
          )
          .build()

        running(application) {

          val request = FakeRequest(GET, routes.MessageController.initiateUpload(DeclarationAmendment).url)

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          verify(mockUpscanConnector).initiate(any[MessageId])(any[HeaderCarrier])

          contentAsString(result) mustEqual message
        }
      }
    }
  }
}
