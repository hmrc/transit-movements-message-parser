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

package config

import com.typesafe.config.ConfigMemorySize
import io.lemonlabs.uri.AbsoluteUrl
import play.api.Configuration
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (val config: Configuration, servicesConfig: ServicesConfig) {
  val objectStoreDirectory: Path.Directory =
    Path.Directory(config.get[String]("object-store.default-directory"))

  val objectStoreRoot: AbsoluteUrl =
    AbsoluteUrl.parse(servicesConfig.baseUrl("object-store"))

  val appName = config.get[String]("appName")

  val selfUrl: AbsoluteUrl =
    AbsoluteUrl.parse(servicesConfig.baseUrl(appName))

  // Upscan

  val upscanInitiateUrl: AbsoluteUrl =
    AbsoluteUrl.parse(servicesConfig.baseUrl("upscan-initiate"))
  val upscanUrl: AbsoluteUrl =
    AbsoluteUrl.parse(servicesConfig.baseUrl("upscan"))
  val upscanMinimumFileSize: Long =
    config.get[ConfigMemorySize]("microservice.services.upscan-initiate.minimum-file-size").toBytes
  val upscanMaximumFileSize: Long =
    config.get[ConfigMemorySize]("microservice.services.upscan-initiate.maximum-file-size").toBytes

  // SDES Stub Direct

  val sdesUrl: String          = servicesConfig.baseUrl("sdes")
  val sdesFilereadyUri: String = config.get[String]("microservice.services.sdes.uri")

  // SDES Proxy

  lazy val sdesProxyUrl: String = servicesConfig.baseUrl("secure-data-exchange-proxy")
  lazy val sdesProxyFileReadyUri: String =
    config.get[String]("microservice.services.secure-data-exchange-proxy.uri")
  lazy val sdesProxyInformationType: String =
    config.get[String]("microservice.services.secure-data-exchange-proxy.information-type")
  lazy val sdesProxySrn: String =
    config.get[String]("microservice.services.secure-data-exchange-proxy.srn")
  lazy val sdesProxyClientId: String =
    config.get[String]("microservice.services.secure-data-exchange-proxy.s")
}
