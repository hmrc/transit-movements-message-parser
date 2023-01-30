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

import base.SpecBase
import generators.ModelGenerators
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.util.UUID

class ConversationIdSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  "ConversationIdSpec#apply(MovementId, MessageId)" - {
    "correctly generates a UUID" in forAll(Gen.long, Gen.long) { (movement, message) =>
      val movementID = MovementId(movement.toHexString.toLowerCase)
      val messageID  = MessageId(message.toHexString.toLowerCase)

      val sut = ConversationId(movementID, messageID)

      sut.value mustBe
        UUID.fromString(
          s"${movementID.value.substring(0, 8)}-${movementID.value.substring(8, 12)}-${movementID.value
            .substring(12)}-${messageID.value.substring(0, 4)}-${messageID.value.substring(4)}"
        )
    }
  }

}
