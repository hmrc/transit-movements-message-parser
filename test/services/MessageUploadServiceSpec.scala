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
import base.SpecBase
import config.AppConfig
import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.Path.File
import uk.gov.hmrc.objectstore.client.config.ObjectStoreClientConfig
import uk.gov.hmrc.objectstore.client.http.ObjectStoreContentWrite
import uk.gov.hmrc.objectstore.client.play._
import uk.gov.hmrc.objectstore.client.{Md5Hash, ObjectSummaryWithMd5, RetentionPeriod}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class MessageUploadServiceSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  private type Write = ObjectStoreContentWrite[FutureEither, ResBody, Request]

  "MessageUploadService" - {
    "uploadFile" - {

      "must return object summary for a successful put" in {
        val mockClient = mock[PlayObjectStoreClientEither]

        val application = baseApplicationBuilder
          .overrides(
            bind[PlayObjectStoreClientEither].toInstance(mockClient)
          )
          .build()

        running(application) {

          val appConfig: AppConfig = application.injector.instanceOf[AppConfig]
          val objectStoreClientConfig: ObjectStoreClientConfig =
            application.injector.instanceOf[ObjectStoreClientConfig]

          forAll(
            arbitrary[String].suchThat(_.nonEmpty),
            arbitrary[String],
            arbitrary[Md5Hash],
            arbitrary[Source[ByteString, NotUsed]],
            arbitrary[ObjectSummaryWithMd5]
          ) { (fileName, fileMimeType, md5Hash, source, response) =>
            reset(mockClient)

            when(
              mockClient.putObject(
                any[File],
                any[Source[ByteString, NotUsed]],
                any[RetentionPeriod],
                any[Option[String]],
                any[Option[Md5Hash]],
                any[String]
              )(
                any[Write],
                any[HeaderCarrier]
              )
            ).thenReturn(Future.successful(Right(response)))

            val service = application.injector.instanceOf[MessageUploadService]

            val result = Await.result(
              service.uploadFile(fileName, fileMimeType, md5Hash, source),
              Duration.Inf
            )

            result.right.get mustBe response

            verify(mockClient).putObject(
              eqTo(appConfig.objectStoreDirectory.file(fileName)),
              eqTo(source),
              eqTo(objectStoreClientConfig.defaultRetentionPeriod),
              eqTo(Some(fileMimeType)),
              eqTo(Some(md5Hash)),
              eqTo(objectStoreClientConfig.owner)
            )(
              any[Write],
              any[HeaderCarrier]
            )
          }
        }
      }

      "must return exception for an unsuccessful put" in {
        val mockClient = mock[PlayObjectStoreClientEither]

        val application = baseApplicationBuilder
          .overrides(
            bind[PlayObjectStoreClientEither].toInstance(mockClient)
          )
          .build()

        running(application) {

          val appConfig: AppConfig = application.injector.instanceOf[AppConfig]
          val objectStoreClientConfig: ObjectStoreClientConfig =
            application.injector.instanceOf[ObjectStoreClientConfig]

          forAll(
            arbitrary[String].suchThat(_.nonEmpty),
            arbitrary[String],
            arbitrary[Md5Hash],
            arbitrary[Source[ByteString, NotUsed]],
            arbitrary[Exception]
          ) { (fileName, fileMimeType, md5Hash, source, exception) =>
            reset(mockClient)

            when(
              mockClient.putObject(
                any[File],
                any[Source[ByteString, NotUsed]],
                any[RetentionPeriod],
                any[Option[String]],
                any[Option[Md5Hash]],
                any[String]
              )(
                any[Write],
                any[HeaderCarrier]
              )
            ).thenReturn(Future.successful(Left(exception)))

            val service = application.injector.instanceOf[MessageUploadService]

            val result = Await.result(
              service.uploadFile(fileName, fileMimeType, md5Hash, source),
              Duration.Inf
            )

            result.left.get mustBe exception

            verify(mockClient).putObject(
              eqTo(appConfig.objectStoreDirectory.file(fileName)),
              eqTo(source),
              eqTo(objectStoreClientConfig.defaultRetentionPeriod),
              eqTo(Some(fileMimeType)),
              eqTo(Some(md5Hash)),
              eqTo(objectStoreClientConfig.owner)
            )(
              any[Write],
              any[HeaderCarrier]
            )
          }
        }
      }
    }
  }
}
