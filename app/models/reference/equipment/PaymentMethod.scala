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

package models.reference.equipment

import cats.Order
import config.FrontendAppConfig
import models.{DynamicEnumerableType, Radioable}
import org.apache.commons.text.StringEscapeUtils
import play.api.libs.json.{__, Format, Json, Reads}
import models.reference.RichComparison
import play.api.libs.functional.syntax.toFunctionalBuilderOps

case class PaymentMethod(method: String, description: String) extends Radioable[PaymentMethod] {

  override val code: String = method

  override def toString: String = StringEscapeUtils.unescapeXml(description)

  override val messageKeyPrefix: String = "equipment.paymentMethod"

}

object PaymentMethod extends DynamicEnumerableType[PaymentMethod] {

  def reads(config: FrontendAppConfig): Reads[PaymentMethod] =
    if (config.phase6Enabled) {
      (
        (__ \ "key").read[String] and
          (__ \ "value").read[String]
      )(PaymentMethod.apply)
    } else {
      Json.reads[PaymentMethod]
    }
  implicit val format: Format[PaymentMethod] = Json.format[PaymentMethod]

  implicit val order: Order[PaymentMethod] = (x: PaymentMethod, y: PaymentMethod) => (x, y).compareBy(_.method)
}
