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

package models.reference.authorisations

import config.Constants._
import models.{DynamicEnumerableType, Radioable}
import org.apache.commons.text.StringEscapeUtils
import play.api.i18n.Messages
import play.api.libs.json.{Format, Json}

case class AuthorisationType(code: String, description: String) extends Radioable[AuthorisationType] {

  override def toString: String = StringEscapeUtils.unescapeXml(description)

  override val messageKeyPrefix: String = AuthorisationType.messageKeyPrefix

  def isACR: Boolean = code == ACR
  def isTRD: Boolean = code == TRD
  def isSSE: Boolean = code == SSE

  def forDisplay(implicit messages: Messages): String =
    code match {
      case ACR => messages(s"${AuthorisationType.messageKeyPrefix}.forDisplay.ACR")
      case SSE => messages(s"${AuthorisationType.messageKeyPrefix}.forDisplay.SSE")
      case TRD => messages(s"${AuthorisationType.messageKeyPrefix}.forDisplay.TRD")
      case _   => code
    }
}

object AuthorisationType extends DynamicEnumerableType[AuthorisationType] {
  implicit val format: Format[AuthorisationType] = Json.format[AuthorisationType]

  val messageKeyPrefix = "authorisations.authorisationType"
}
