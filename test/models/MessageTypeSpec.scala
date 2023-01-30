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

package models

import base.SpecBase
import generators.ModelGenerators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc.PathBindable
import org.scalacheck.Arbitrary.arbitrary

class MessageTypeSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  "MessageType" - {

    "RequestMessageType" - {

      val pathBindable = implicitly[PathBindable[RequestMessageType]]

      "must bind valid values by message type code" in {
        forAll(arbitrary[RequestMessageType]) { messageType =>
          val bind = pathBindable.bind("messageType", messageType.code)
          bind.value mustBe messageType
        }
      }

      "must not bind invalid values" in {
        forAll(arbitrary[String]) { code =>
          val bind = pathBindable.bind("messageType", code)
          bind.isLeft mustBe true
        }
      }

      "must unbind path" in {
        forAll(arbitrary[RequestMessageType]) { messageType =>
          val bindValue = pathBindable.unbind("messageType", messageType)
          bindValue mustBe messageType.code
        }
      }
    }
  }
}
