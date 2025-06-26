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

package models.reference.transportMeans.active

import cats.Order
import config.FrontendAppConfig
import models.reference.RichComparison
import models.{DynamicEnumerableType, Radioable}
import org.apache.commons.text.StringEscapeUtils
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{__, Format, Json, Reads}

case class Identification(code: String, description: String) extends Radioable[Identification] {

  override def toString: String = StringEscapeUtils.unescapeXml(description)

  override val messageKeyPrefix: String = "transportMeans.active.identification"

}

object Identification extends DynamicEnumerableType[Identification] {

  def reads(config: FrontendAppConfig): Reads[Identification] =
    if (config.phase6Enabled) {
      (
        (__ \ "key").read[String] and
          (__ \ "value").read[String]
      )(Identification.apply)
    } else {
      Json.reads[Identification]
    }
  implicit val format: Format[Identification] = Json.format[Identification]

  implicit val order: Order[Identification] = (x: Identification, y: Identification) => (x, y).compareBy(_.code)
}
