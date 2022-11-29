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

package models.sdes

case class SdesCallback(
  notification: String,
  filename: String,
  checksumAlgorithm: String,
  checksum: String,
  correlationID: String,
  availableUntil: String, //"2021-01-06T10:01:00.889Z"
  failureReason: String,  //"Virus Detected",
  dateTime: String        //"2021-01-01T10:01:00.889Z"
)
//"properties": [
//{
//"name": "name1",
//"value": "value1"
//},
//{
//"name": "name2",
//"value": "value2"
//}
//]