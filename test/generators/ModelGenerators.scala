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

package generators

import io.lemonlabs.uri.AbsoluteUrl
import models.upscan._
import models.values.{MessageId, UpscanReference}
import models.{MessageType, RequestMessageType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.objectstore.client.Md5Hash

import java.time.Instant
import java.util.UUID

trait ModelGenerators {

  implicit lazy val arbitraryUpscanInitiateResponse: Arbitrary[UpscanInitiateResponse] =
    Arbitrary {
      for {
        upscanReference <- arbitraryUpscanReference.arbitrary
        href            <- arbitraryAbsoluteUrl.arbitrary
        fields          <- arbitraryFields.arbitrary
      } yield UpscanInitiateResponse(
        upscanReference,
        UpscanFormTemplate(href, fields)
      )
    }

  type Fields = Map[String, String]
  implicit lazy val arbitraryFields: Arbitrary[Fields] = {
    Arbitrary {
      for {
        values <- Gen.listOf(Gen.alphaNumStr)
        keys   <- Gen.containerOfN[List, String](values.size, Gen.alphaNumStr)
      } yield keys.zip(values).toMap
    }
  }

  implicit lazy val arbitraryUpscanReference: Arbitrary[UpscanReference] =
    Arbitrary {
      for {
        value <- Gen.alphaNumStr
      } yield UpscanReference(value)
    }

  implicit lazy val arbitraryAbsoluteUrl: Arbitrary[AbsoluteUrl] = {
    Arbitrary {
      for {
        protocol <- Gen.oneOf("http", "https")
        domain   <- Gen.alphaNumStr
        tld      <- Gen.oneOf("com", "io", "net")
        path     <- Gen.alphaNumStr
      } yield AbsoluteUrl.parse(s"$protocol://$domain.$tld/$path")
    }
  }

  implicit lazy val arbitraryRequestMessageType: Arbitrary[RequestMessageType] =
    Arbitrary {
      Gen.oneOf(MessageType.requestValues)
    }

  implicit lazy val arbitraryMessageId: Arbitrary[MessageId] =
    Arbitrary {
      for {
        value <- arbitrary[UUID]
      } yield MessageId(value)
    }

  implicit lazy val arbitraryUpstreamErrorResponse: Arbitrary[UpstreamErrorResponse] =
    Arbitrary {
      for {
        message    <- Gen.alphaNumStr
        statusCode <- Gen.choose(400, 599)
      } yield UpstreamErrorResponse(message, statusCode)
    }

  implicit lazy val arbitraryUpscanNotification: Arbitrary[UpscanNotification] =
    Arbitrary {
      for {
        success      <- arbitraryUpscanSuccessNotification.arbitrary
        failure      <- arbitraryUpscanFailureNotification.arbitrary
        notification <- Gen.oneOf(success, failure)
      } yield notification
    }

  implicit lazy val arbitraryUpscanSuccessNotification: Arbitrary[UpscanSuccessNotification] =
    Arbitrary {
      for {
        reference     <- arbitraryUpscanReference.arbitrary
        downloadUrl   <- arbitraryAbsoluteUrl.arbitrary
        uploadDetails <- arbitraryUpscanUploadDetails.arbitrary
      } yield UpscanSuccessNotification(reference, downloadUrl, Ready, uploadDetails)
    }

  implicit lazy val arbitraryUpscanFailureNotification: Arbitrary[UpscanFailureNotification] =
    Arbitrary {
      for {
        reference      <- arbitraryUpscanReference.arbitrary
        failureDetails <- arbitraryUpscanFailureDetails.arbitrary
      } yield UpscanFailureNotification(reference, Failed, failureDetails)
    }

  implicit lazy val arbitraryUpscanUploadDetails: Arbitrary[UpscanUploadDetails] =
    Arbitrary {
      for {
        fileName        <- Gen.alphaNumStr
        fileMimeType    <- Gen.alphaNumStr
        uploadTimestamp <- arbitrary[Instant]
        checksum        <- arbitraryMd5Hash.arbitrary
        size            <- arbitrary[Long]
      } yield UpscanUploadDetails(fileName, fileMimeType, uploadTimestamp, checksum, size)
    }

  implicit lazy val arbitraryUpscanFailureDetails: Arbitrary[UpscanFailureDetails] =
    Arbitrary {
      for {
        failureReason <- arbitraryUpscanFailureReason.arbitrary
        message       <- Gen.alphaNumStr
      } yield UpscanFailureDetails(failureReason, message)
    }

  implicit lazy val arbitraryMd5Hash: Arbitrary[Md5Hash] =
    Arbitrary {
      for {
        value <- Gen.alphaNumStr
      } yield Md5Hash(value)
    }

  implicit lazy val arbitraryUpscanFailureReason: Arbitrary[UpscanFailureReason] =
    Arbitrary {
      Gen.oneOf(UpscanFailureReason.values)
    }
}
