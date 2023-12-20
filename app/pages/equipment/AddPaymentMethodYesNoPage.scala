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

import controllers.equipment.routes
import models.{Mode, RichJsArray, UserAnswers}
import pages.QuestionPage
import pages.external._
import pages.sections.equipment.EquipmentsAndChargesSection
import pages.sections.external.ItemsSection
import play.api.libs.json.{JsArray, JsPath}
import play.api.mvc.Call

import scala.util.{Success, Try}

case object AddPaymentMethodYesNoPage extends QuestionPage[Boolean] {

  override def path: JsPath = EquipmentsAndChargesSection.path \ toString

  override def toString: String = "addPaymentMethodYesNo"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.AddPaymentMethodYesNoController.onPageLoad(userAnswers.lrn, mode))

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(false) => userAnswers.remove(PaymentMethodPage)
      case Some(true)  => removeItemLevelTransportCharges(userAnswers)
      case _           => super.cleanup(value, userAnswers)
    }

  private def removeItemLevelTransportCharges(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers
      .get(ItemsSection)
      .getOrElse(JsArray())
      .zipWithIndex
      .foldLeft[Try[UserAnswers]](Success(userAnswers)) {
        case (acc, (_, index)) =>
          acc.map(_.remove(ItemAddTransportChargesYesNoPage(index)).remove(ItemTransportChargesPage(index)))
      }
}
