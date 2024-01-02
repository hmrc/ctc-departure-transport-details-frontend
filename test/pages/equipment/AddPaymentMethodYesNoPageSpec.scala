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

package pages.equipment

import models.Index
import models.reference.equipment.PaymentMethod
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.external._
import play.api.libs.json.{JsObject, Json}

class AddPaymentMethodYesNoPageSpec extends PageBehaviours {

  "AddPaymentMethodYesNoPage" - {

    beRetrievable[Boolean](AddPaymentMethodYesNoPage)

    beSettable[Boolean](AddPaymentMethodYesNoPage)

    beRemovable[Boolean](AddPaymentMethodYesNoPage)

    "cleanup" - {
      "when no selected" - {
        "must remove payment method" in {
          forAll(arbitrary[PaymentMethod]) {
            paymentMethod =>
              val userAnswers = emptyUserAnswers
                .setValue(AddPaymentMethodYesNoPage, true)
                .setValue(PaymentMethodPage, paymentMethod)

              val result = userAnswers.setValue(AddPaymentMethodYesNoPage, false)

              result.get(PaymentMethodPage) must not be defined
          }
        }
      }

      "when yes selected" - {
        "must remove transport charges for each item" in {
          forAll(arbitrary[PaymentMethod]) {
            paymentMethod =>
              val userAnswers = emptyUserAnswers
                .setValue(ItemAddTransportChargesYesNoPage(Index(0)), true)
                .setValue(ItemTransportChargesPage(Index(0)), Json.toJson(paymentMethod).as[JsObject])
                .setValue(ItemAddTransportChargesYesNoPage(Index(1)), true)
                .setValue(ItemTransportChargesPage(Index(1)), Json.toJson(paymentMethod).as[JsObject])

              val result = userAnswers.setValue(AddPaymentMethodYesNoPage, true)

              result.get(ItemAddTransportChargesYesNoPage(Index(0))) must not be defined
              result.get(ItemTransportChargesPage(Index(0))) must not be defined

              result.get(ItemAddTransportChargesYesNoPage(Index(1))) must not be defined
              result.get(ItemTransportChargesPage(Index(1))) must not be defined
          }
        }
      }
    }
  }
}
