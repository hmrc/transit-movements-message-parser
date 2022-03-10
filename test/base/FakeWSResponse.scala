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

package base

import akka.stream.scaladsl.Source
import akka.util.ByteString
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSCookie, WSResponse}

import java.net.URI
import scala.xml.Elem

abstract class FakeWSResponse(statusCode: Int) extends WSResponse {
  override def status: Int = statusCode

  override def statusText: String = ???

  override def headers: Map[String, Seq[String]] = ???

  override def underlying[T]: T = ???

  override def cookies: Seq[WSCookie] = ???

  override def cookie(name: String): Option[WSCookie] = ???

  override def body: String = ???

  override def bodyAsBytes: ByteString = ???

  override def bodyAsSource: Source[ByteString, _] = ???

  override def allHeaders: Map[String, Seq[String]] = ???

  override def xml: Elem = ???

  override def json: JsValue = ???

  override def uri: URI = ???
}

class SuccessWSResponse(statusCode: Int, source: Source[ByteString, _])
  extends FakeWSResponse(statusCode) {
  override def bodyAsSource: Source[ByteString, _] = source
}

class FailureWSResponse(statusCode: Int, bodyString: String, headersMap: Map[String, Seq[String]])
  extends FakeWSResponse(statusCode) {
  override def headers: Map[String, Seq[String]] = headersMap

  override def body: String = bodyString
}
