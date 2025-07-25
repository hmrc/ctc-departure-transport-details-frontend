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

package models.reference

import cats.Order
import config.FrontendAppConfig
import models.Selectable
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{__, Json, OFormat, Reads}

case class Country(code: CountryCode, description: String) extends Selectable {
  override def toString: String = s"$description - ${code.code}"

  override val value: String = code.code
}

object Country {

  def reads(config: FrontendAppConfig): Reads[Country] =
    if (config.phase6Enabled) {
      (
        (__ \ "key").read[CountryCode] and
          (__ \ "value").read[String]
      )(Country.apply)
    } else {
      Json.reads[Country]
    }

  implicit val format: OFormat[Country] = Json.format[Country]

  implicit val order: Order[Country] = (x: Country, y: Country) => (x, y).compareBy(_.description, _.code.code)

  def queryParameters(code: String)(config: FrontendAppConfig): Seq[(String, String)] =
    val key = if (config.phase6Enabled) "keys" else "data.code"
    Seq(key -> code)

}
