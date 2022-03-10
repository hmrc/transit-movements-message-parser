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

package models.formats

import base.SpecBase
import generators.ModelGenerators
import io.lemonlabs.uri.AbsoluteUrl
import models.upscan._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.objectstore.client.Md5Hash

class HttpFormatsSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  "HttpFormats" - {

    "absoluteUrlFormat" - {
      "must read from json and write to json" in {
        forAll(arbitrary[AbsoluteUrl]) { absoluteUrl =>
          val json = Json.parse(s""""${absoluteUrl.toString}"""")

          json.as[AbsoluteUrl] mustBe absoluteUrl
          Json.toJson(absoluteUrl) mustBe json
        }
      }
    }

    "md5HashFormat" - {
      "must read from json and write to json" in {
        forAll(arbitrary[Md5Hash]) { md5Hash =>
          val json = Json.parse(s""""${md5Hash.value}"""")

          json.as[Md5Hash] mustBe md5Hash
          Json.toJson(md5Hash) mustBe json
        }
      }
    }

    "upscanFileStatusFormat" - {
      "must read from json and write to json" in {
        forAll(arbitrary[UpscanFileStatus]) { upscanFileStatus =>
          val json = Json.parse(s""""${upscanFileStatus.name}"""")

          json.as[UpscanFileStatus] mustBe upscanFileStatus
          Json.toJson(upscanFileStatus) mustBe json
        }
      }
    }

    "upscanFailureReasonFormat" - {
      "must read from json and write to json" in {
        forAll(arbitrary[UpscanFailureReason]) { upscanFailureReason =>
          val json = Json.parse(s""""${upscanFailureReason.name}"""")

          json.as[UpscanFailureReason] mustBe upscanFailureReason
          Json.toJson(upscanFailureReason) mustBe json
        }
      }
    }

    "upscanUploadDetailsFormat" - {
      "must read from json and write to json" in {
        forAll(arbitrary[UpscanUploadDetails]) { upscanUploadDetails =>
          val json = Json.parse(s"""
               |{
               |  "fileName": "${upscanUploadDetails.fileName}",
               |  "fileMimeType": "${upscanUploadDetails.fileMimeType}",
               |  "uploadTimestamp": "${upscanUploadDetails.uploadTimestamp}",
               |  "checksum": "${upscanUploadDetails.checksum.value}",
               |  "size": ${upscanUploadDetails.size}
               |}
               |""".stripMargin)

          json.as[UpscanUploadDetails] mustBe upscanUploadDetails
          Json.toJson(upscanUploadDetails) mustBe json
        }
      }
    }

    "upscanFailureDetailsFormat" - {
      "must read from json and write to json" in {
        forAll(arbitrary[UpscanFailureDetails]) { upscanFailureDetails =>
          val json = Json.parse(s"""
               |{
               |  "failureReason": "${upscanFailureDetails.failureReason.name}",
               |  "message": "${upscanFailureDetails.message}"
               |}
               |""".stripMargin)

          json.as[UpscanFailureDetails] mustBe upscanFailureDetails
          Json.toJson(upscanFailureDetails) mustBe json
        }
      }
    }

    "upscanSuccessNotificationFormat" - {
      "must read from json and write to json" in {
        forAll(arbitrary[UpscanSuccessNotification]) { upscanSuccessNotification =>
          val json = Json.parse(s"""
               |{
               |  "reference": "${upscanSuccessNotification.reference.value}",
               |  "downloadUrl": "${upscanSuccessNotification.downloadUrl.toString}",
               |  "fileStatus": "${upscanSuccessNotification.fileStatus.name}",
               |  "uploadDetails": {
               |    "fileName": "${upscanSuccessNotification.uploadDetails.fileName}",
               |    "fileMimeType": "${upscanSuccessNotification.uploadDetails.fileMimeType}",
               |    "uploadTimestamp": "${upscanSuccessNotification.uploadDetails.uploadTimestamp}",
               |    "checksum": "${upscanSuccessNotification.uploadDetails.checksum.value}",
               |    "size": ${upscanSuccessNotification.uploadDetails.size}
               |  }
               |}
               |""".stripMargin)

          json.as[UpscanSuccessNotification] mustBe upscanSuccessNotification
          Json.toJson(upscanSuccessNotification) mustBe json
        }
      }
    }

    "upscanFailureNotificationFormat" - {
      "must read from json and write to json" in {
        forAll(arbitrary[UpscanFailureNotification]) { upscanFailureNotification =>
          val json = Json.parse(s"""
               |{
               |  "reference": "${upscanFailureNotification.reference.value}",
               |  "fileStatus": "${upscanFailureNotification.fileStatus.name}",
               |  "failureDetails": {
               |    "failureReason": "${upscanFailureNotification.failureDetails.failureReason.name}",
               |    "message": "${upscanFailureNotification.failureDetails.message}"
               |  }
               |}
               |""".stripMargin)

          json.as[UpscanFailureNotification] mustBe upscanFailureNotification
          Json.toJson(upscanFailureNotification) mustBe json
        }
      }
    }

    "upscanNotificationFormat" - {
      "must read from json and write to json" - {
        "when UpscanSuccessNotification" in {
          forAll(arbitrary[UpscanSuccessNotification]) { upscanNotification =>
            val json = Json.parse(s"""
                 |{
                 |  "reference": "${upscanNotification.reference.value}",
                 |  "downloadUrl": "${upscanNotification.downloadUrl.toString}",
                 |  "fileStatus": "${upscanNotification.fileStatus.name}",
                 |  "uploadDetails": {
                 |    "fileName": "${upscanNotification.uploadDetails.fileName}",
                 |    "fileMimeType": "${upscanNotification.uploadDetails.fileMimeType}",
                 |    "uploadTimestamp": "${upscanNotification.uploadDetails.uploadTimestamp}",
                 |    "checksum": "${upscanNotification.uploadDetails.checksum.value}",
                 |    "size": ${upscanNotification.uploadDetails.size}
                 |  }
                 |}
                 |""".stripMargin)

            json.as[UpscanNotification] mustBe upscanNotification
            Json.toJson(upscanNotification) mustBe json
          }
        }

        "when UpscanFailureNotification" in {
          forAll(arbitrary[UpscanFailureNotification]) { upscanNotification =>
            val json = Json.parse(s"""
                 |{
                 |  "reference": "${upscanNotification.reference.value}",
                 |  "fileStatus": "${upscanNotification.fileStatus.name}",
                 |  "failureDetails": {
                 |    "failureReason": "${upscanNotification.failureDetails.failureReason.name}",
                 |    "message": "${upscanNotification.failureDetails.message}"
                 |  }
                 |}
                 |""".stripMargin)

            json.as[UpscanNotification] mustBe upscanNotification
            Json.toJson(upscanNotification) mustBe json
          }
        }
      }
    }

    "upscanInitiateRequestFormat" - {
      "must read from json and write to json" in {
        forAll(arbitrary[UpscanInitiateRequest]) { upscanInitiateRequest =>
          val json = Json.parse(s"""
               |{
               |  "callbackUrl": "${upscanInitiateRequest.callbackUrl.toString}",
               |  "successRedirect": "${upscanInitiateRequest.successRedirect.toString}",
               |  "errorRedirect": "${upscanInitiateRequest.errorRedirect.toString}",
               |  "minimumFileSize": ${upscanInitiateRequest.minimumFileSize},
               |  "maximumFileSize": ${upscanInitiateRequest.maximumFileSize}
               |}
               |""".stripMargin)

          json.as[UpscanInitiateRequest] mustBe upscanInitiateRequest
          Json.toJson(upscanInitiateRequest) mustBe json
        }
      }
    }

    "upscanFormTemplateFormat" - {
      "must read from json and write to json" in {
        forAll(arbitrary[UpscanFormTemplate]) { upscanFormTemplate =>
          val json = Json.parse(s"""
               |{
               |  "href": "${upscanFormTemplate.href.toString}",
               |  "fields": ${Json.toJson(upscanFormTemplate.fields)}
               |}
               |""".stripMargin)

          json.as[UpscanFormTemplate] mustBe upscanFormTemplate
          Json.toJson(upscanFormTemplate) mustBe json
        }
      }
    }

    "upscanInitiateResponseFormat" - {
      "must read from json and write to json" in {
        forAll(arbitrary[UpscanInitiateResponse]) { upscanInitiateResponse =>
          val json = Json.parse(s"""
               |{
               |  "reference": "${upscanInitiateResponse.reference.value}",
               |  "uploadRequest": {
               |    "href": "${upscanInitiateResponse.uploadRequest.href.toString}",
               |    "fields": ${Json.toJson(upscanInitiateResponse.uploadRequest.fields)}
               |  }
               |}
               |""".stripMargin)

          json.as[UpscanInitiateResponse] mustBe upscanInitiateResponse
          Json.toJson(upscanInitiateResponse) mustBe json
        }
      }
    }
  }
}
