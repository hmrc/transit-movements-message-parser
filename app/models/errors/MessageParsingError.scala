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

package models.errors

import cats.data.NonEmptyList
import models.MessageType
import uk.gov.hmrc.http.UpstreamErrorResponse

sealed abstract class MessageParsingError extends Product with Serializable

case class BadRequestError(message: String) extends MessageParsingError

case class NotFoundError(message: String) extends MessageParsingError

case class XmlValidationError(messageType: MessageType, errors: NonEmptyList[SchemaValidationError])
  extends MessageParsingError {
  lazy val message: String = s"Error while validating ${messageType.code} message"
}

case class UpstreamServiceError(
  message: String = "Internal server error",
  cause: UpstreamErrorResponse
) extends MessageParsingError

object UpstreamServiceError {
  def causedBy(cause: UpstreamErrorResponse): MessageParsingError =
    MessageParsingError.upstreamServiceError(cause = cause)
}

case class InternalServiceError(
  message: String = "Internal server error",
  cause: Option[Throwable] = None
) extends MessageParsingError

object InternalServiceError {
  def causedBy(cause: Throwable): MessageParsingError =
    MessageParsingError.internalServiceError(cause = Some(cause))
}

object MessageParsingError {
  def badRequestError(message: String): MessageParsingError =
    BadRequestError(message)

  def notFoundError(message: String): MessageParsingError =
    NotFoundError(message)

  def upstreamServiceError(
    message: String = "Internal server error",
    cause: UpstreamErrorResponse
  ): MessageParsingError =
    UpstreamServiceError(message, cause)

  def internalServiceError(
    message: String = "Internal server error",
    cause: Option[Throwable] = None
  ): MessageParsingError =
    InternalServiceError(message, cause)
}
