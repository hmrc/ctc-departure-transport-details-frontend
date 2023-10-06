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

package models.journeyDomain.equipment

import cats.implicits._
import config.Constants.SecurityType._
import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.JourneyDomainModel
import models.reference.equipment.PaymentMethod
import pages.equipment._
import pages.external.SecurityDetailsTypePage
import pages.preRequisites.ContainerIndicatorPage

case class EquipmentsAndChargesDomain(
  equipments: Option[EquipmentsDomain],
  paymentMethod: Option[PaymentMethod]
) extends JourneyDomainModel

object EquipmentsAndChargesDomain {

  implicit val userAnswersReader: UserAnswersReader[EquipmentsAndChargesDomain] = (
    equipmentsReader,
    chargesReader
  ).tupled.map((EquipmentsAndChargesDomain.apply _).tupled)

  implicit lazy val equipmentsReader: UserAnswersReader[Option[EquipmentsDomain]] =
    ContainerIndicatorPage.reader.map(_.value).flatMap {
      case Some(true) =>
        UserAnswersReader[EquipmentsDomain].map(Option(_))
      case Some(false) =>
        AddTransportEquipmentYesNoPage.filterOptionalDependent(identity) {
          UserAnswersReader[EquipmentsDomain]
        }
      case None =>
        none[EquipmentsDomain].pure[UserAnswersReader]
    }

  implicit lazy val chargesReader: UserAnswersReader[Option[PaymentMethod]] = SecurityDetailsTypePage.reader.flatMap {
    case NoSecurityDetails => none[PaymentMethod].pure[UserAnswersReader]
    case _                 => AddPaymentMethodYesNoPage.filterOptionalDependent(identity)(PaymentMethodPage.reader)
  }
}
