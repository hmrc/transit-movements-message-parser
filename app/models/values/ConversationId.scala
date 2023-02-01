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

package models.values

import play.api.libs.json.Format
import play.api.libs.json.Json

import java.util.UUID

object ConversationId {
  implicit val conversationIdFormat: Format[ConversationId] = Json.valueFormat[ConversationId]

  // 123e4567-e89b-12d3-a456-426614174000
  def apply(movementId: MovementId, messageId: MessageId): ConversationId =
    ConversationId(
      UUID.fromString(
        s"${movementId.value.substring(0, 8)}-${movementId.value.substring(8, 12)}-${movementId.value
          .substring(12)}-${messageId.value.substring(0, 4)}-${messageId.value.substring(4)}"
      )
    )
}

case class ConversationId(value: UUID)
