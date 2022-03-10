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

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, post, urlEqualTo}
import config.AppConfig
import generators.ModelGenerators
import io.lemonlabs.uri.AbsoluteUrl
import models.upscan.{UpscanInitiateRequest, UpscanInitiateResponse}
import models.values.MessageId
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status.OK
import play.api.libs.json.{Json, Writes}
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class UpscanConnectorSpec
  extends SpecBase
  with WiremockSuite
  with ScalaCheckPropertyChecks
  with ModelGenerators {

  override protected def portConfigKey: String = "microservice.services.upscan-initiate.port"

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private def stringify[T](value: T)(implicit writes: Writes[T]): String =
    Json.stringify(Json.toJson(value))

  "UpscanConnector" - {

    "initiate" - {

      val url = "/upscan/v2/initiate"

      def request(uuid: UUID)(implicit appConfig: AppConfig): UpscanInitiateRequest =
        UpscanInitiateRequest(
          callbackUrl = AbsoluteUrl.parse(s"$baseUrl/scan-complete/$uuid"),
          successRedirect = AbsoluteUrl.parse(s"$baseUrl/upload-success/$uuid"),
          errorRedirect = AbsoluteUrl.parse(s"$baseUrl/upload-failure/$uuid"),
          minimumFileSize = appConfig.upscanMinimumFileSize,
          maximumFileSize = appConfig.upscanMaximumFileSize
        )

      "must return upscan initiate response for a 200" in {

        val app = appBuilder.build()

        running(app) {
          val connector                     = app.injector.instanceOf[UpscanConnector]
          implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

          forAll(arbitrary[MessageId], arbitrary[UpscanInitiateResponse]) { (messageId, response) =>
            server.stubFor(
              post(urlEqualTo(url))
                .withRequestBody(equalToJson(stringify(request(messageId.value))))
                .willReturn(
                  aResponse()
                    .withStatus(OK)
                    .withBody(stringify(response))
                )
            )

            val result = Await.result(connector.initiate(messageId), Duration.Inf)
            result.right.get mustBe response
          }
        }
      }

      "must return upstream error response for a 4xx/5xx" in {

        val app = appBuilder.build()

        running(app) {
          val connector                     = app.injector.instanceOf[UpscanConnector]
          implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

          forAll(arbitrary[MessageId], Gen.chooseNum(400, 599)) { (messageId, statusCode) =>
            server.stubFor(
              post(urlEqualTo(url))
                .withRequestBody(equalToJson(stringify(request(messageId.value))))
                .willReturn(
                  aResponse()
                    .withStatus(statusCode)
                )
            )

            val result = Await.result(connector.initiate(messageId), Duration.Inf)
            result.left.get.statusCode mustBe statusCode
          }
        }
      }
    }
  }
}
