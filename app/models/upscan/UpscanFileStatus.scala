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

package models.upscan

sealed abstract class UpscanFileStatus(val name: String) extends Product with Serializable

case object Ready  extends UpscanFileStatus("READY")
case object Failed extends UpscanFileStatus("FAILED")

object UpscanFileStatus {
  val values: Set[UpscanFileStatus] = Set(Ready, Failed)
}
