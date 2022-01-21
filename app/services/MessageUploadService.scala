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

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.util.ByteString
import config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.Md5Hash
import uk.gov.hmrc.objectstore.client.ObjectSummaryWithMd5
import uk.gov.hmrc.objectstore.client.play.Implicits._
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClientEither

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class MessageUploadService @Inject() (
  objectStore: PlayObjectStoreClientEither,
  appConfig: AppConfig
)(implicit system: ActorSystem) {

  implicit val executionContext: ExecutionContext =
    system.dispatcher

  def uploadFile(
    fileName: String,
    fileMimeType: String,
    fileChecksum: Md5Hash,
    fileData: Source[ByteString, _]
  )(implicit hc: HeaderCarrier): Future[Either[Exception, ObjectSummaryWithMd5]] = {
    objectStore.putObject(
      path = appConfig.objectStoreDirectory.file(fileName),
      content = fileData,
      contentType = Some(fileMimeType),
      contentMd5 = Some(fileChecksum)
    )
  }
}
