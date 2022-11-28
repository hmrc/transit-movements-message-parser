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

sealed trait SdesNotification {
  def asString: String
}

case object FileReady extends SdesNotification {
  override def asString: String = "FileReady"
}

case object FileReceived extends SdesNotification {
  override def asString: String = "FileReceived"
}

case class FileProcessingFailure(errorMessage: String) extends SdesNotification {
  override def asString: String = "FileProcessingFailure"
}

case object FileProcessed extends SdesNotification {
  override def asString: String = "FileProcessed"
}

object SdesNotification {
  def parse(s: String): Option[SdesNotification] = {
    s match {
      case "FileReady"             => Some(FileReady)
      case "FileReceived"          => Some(FileReceived)
      case "FileProcessingFailure" => Some(FileProcessingFailure(""))
      case "FileProcessed"         => Some(FileProcessed)
      case _                       => None
    }
  }
}
