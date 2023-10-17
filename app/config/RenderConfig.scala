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

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.StringContextOps

import java.net.URLEncoder

@Singleton
class RenderConfig @Inject() (configuration: Configuration) {

  private val contactHost: String                  = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier: String = "CTCTraders"
  private val host: String                         = configuration.get[String]("host")
  private val encodeURL: String => String = URLEncoder.encode(_: String, "UTF-8")

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${encodeURL(host + request.uri)}"

  val timeoutSeconds: Int   = configuration.get[Int]("session.timeoutSeconds")
  val countdownSeconds: Int = configuration.get[Int]("session.countdownSeconds")

  val showUserResearchBanner: Boolean = configuration.get[Boolean]("banners.showUserResearch")
  val userResearchUrl: String         = configuration.get[String]("urls.userResearch")
}
