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

package controllers

import akka.stream.Materializer
import akka.stream.scaladsl.FileIO
import play.api.mvc.{BodyParserUtils, PlayBodyParsers}

trait StreamingBodyParser extends BodyParserUtils {
  implicit class StreamingBodyParserOps(parsers: PlayBodyParsers)(implicit mat: Materializer) {
    def stream = parsers.temporaryFile.map { tempFile =>
      FileIO.fromPath(tempFile.path)
    }(mat.executionContext)
  }
}
