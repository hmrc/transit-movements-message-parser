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
import io.lemonlabs.uri.UrlPath
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

  // SDES
  lazy val useProxy: Boolean = config.get[Boolean]("sdes.use-proxy")
  lazy val service: String   = if (useProxy) "secure-data-exchange-proxy" else "sdes-stub"

  lazy val sdesInformationType: String = config.get[String]("sdes.information-type")
  lazy val sdesSrn: String             = config.get[String]("sdes.srn")
  lazy val sdesClientId: String        = config.get[String]("sdes.client-id")

  lazy val objectStoreUrl: String =
    config.get[String]("microservice.services.object-store.sdes-host")

  // SDES Proxy

  lazy val sdesUrl: AbsoluteUrl =
    AbsoluteUrl.parse(servicesConfig.baseUrl(service))
  lazy val sdesFileReadyUri: UrlPath =
    UrlPath(
      config
        .get[String](s"microservice.services.$service.file-ready-uri")
        .split("/")
        .filter(_.nonEmpty)
    )

}
