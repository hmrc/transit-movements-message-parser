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

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.lemonlabs.uri.AbsoluteUrl
import models.upscan._
import models.values.{MovementId, UpscanReference}
import models.{MessageType, RequestMessageType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.objectstore.client.{Md5Hash, ObjectSummaryWithMd5}
import uk.gov.hmrc.objectstore.client.Path.{Directory, File}

import java.time.Instant
import java.util.UUID

trait ModelGenerators {

  implicit lazy val arbitraryUpscanInitiateResponse: Arbitrary[UpscanInitiateResponse] =
    Arbitrary {
      for {
        upscanReference <- arbitrary[UpscanReference]
        uploadRequest   <- arbitrary[UpscanFormTemplate]
      } yield UpscanInitiateResponse(upscanReference, uploadRequest)
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

  implicit lazy val arbitraryHeaders: Arbitrary[Map[String, Seq[String]]] = {
    Arbitrary {
      for {
        values <- Gen.listOf(Gen.listOf(Gen.alphaNumStr))
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

  implicit lazy val arbitraryMessageId: Arbitrary[MovementId] =
    Arbitrary {
      for {
        value <- arbitrary[UUID]
      } yield MovementId(value)
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
        success      <- arbitrary[UpscanSuccessNotification]
        failure      <- arbitrary[UpscanFailureNotification]
        notification <- Gen.oneOf(success, failure)
      } yield notification
    }

  implicit lazy val arbitraryUpscanSuccessNotification: Arbitrary[UpscanSuccessNotification] =
    Arbitrary {
      for {
        reference     <- arbitrary[UpscanReference]
        downloadUrl   <- arbitrary[AbsoluteUrl]
        uploadDetails <- arbitrary[UpscanUploadDetails]
      } yield UpscanSuccessNotification(reference, downloadUrl, Ready, uploadDetails)
    }

  implicit lazy val arbitraryUpscanFailureNotification: Arbitrary[UpscanFailureNotification] =
    Arbitrary {
      for {
        reference      <- arbitrary[UpscanReference]
        failureDetails <- arbitrary[UpscanFailureDetails]
      } yield UpscanFailureNotification(reference, Failed, failureDetails)
    }

  implicit lazy val arbitraryUpscanUploadDetails: Arbitrary[UpscanUploadDetails] =
    Arbitrary {
      for {
        fileName        <- Gen.alphaNumStr
        fileMimeType    <- Gen.alphaNumStr
        uploadTimestamp <- arbitrary[Instant]
        checksum        <- arbitrary[Md5Hash]
        size            <- arbitrary[Long]
      } yield UpscanUploadDetails(fileName, fileMimeType, uploadTimestamp, checksum, size)
    }

  implicit lazy val arbitraryUpscanFailureDetails: Arbitrary[UpscanFailureDetails] =
    Arbitrary {
      for {
        failureReason <- arbitrary[UpscanFailureReason]
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

  implicit lazy val arbitrarySource: Arbitrary[Source[ByteString, NotUsed]] =
    Arbitrary {
      for {
        values <- Gen.listOf(Gen.alphaNumStr)
      } yield Source(values).map(ByteString(_))
    }

  implicit lazy val arbitraryObjectSummaryWithMd5: Arbitrary[ObjectSummaryWithMd5] =
    Arbitrary {
      for {
        file          <- arbitrary[File]
        contentLength <- arbitrary[Long]
        contentMd5    <- arbitrary[Md5Hash]
        lastModified  <- arbitrary[Instant]
      } yield ObjectSummaryWithMd5(file, contentLength, contentMd5, lastModified)
    }

  implicit lazy val arbitraryFile: Arbitrary[File] =
    Arbitrary {
      for {
        value    <- Gen.alphaNumStr
        fileName <- Gen.alphaNumStr.suchThat(_.nonEmpty)
      } yield File(Directory(value), fileName)
    }

  implicit lazy val arbitraryUpscanFormTemplate: Arbitrary[UpscanFormTemplate] =
    Arbitrary {
      for {
        href   <- arbitrary[AbsoluteUrl]
        fields <- arbitrary[Fields]
      } yield UpscanFormTemplate(href, fields)
    }

  implicit lazy val arbitraryUpscanFileStatus: Arbitrary[UpscanFileStatus] =
    Arbitrary {
      Gen.oneOf(UpscanFileStatus.values)
    }

  implicit lazy val arbitraryUpscanInitiateRequest: Arbitrary[UpscanInitiateRequest] =
    Arbitrary {
      for {
        callbackUrl     <- arbitrary[AbsoluteUrl]
        minimumFileSize <- arbitrary[Long]
        maximumFileSize <- arbitrary[Long]
      } yield UpscanInitiateRequest(
        callbackUrl,
        minimumFileSize,
        maximumFileSize
      )
    }
}
