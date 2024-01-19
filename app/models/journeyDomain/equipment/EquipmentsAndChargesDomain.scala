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
import models.domain._
import models.journeyDomain.{JourneyDomainModel, ReaderSuccess}
import models.reference.equipment.PaymentMethod
import pages.equipment._
import pages.external.SecurityDetailsTypePage
import pages.preRequisites.ContainerIndicatorPage

case class EquipmentsAndChargesDomain(
  equipments: Option[EquipmentsDomain],
  paymentMethod: Option[PaymentMethod]
) extends JourneyDomainModel

object EquipmentsAndChargesDomain {

  implicit val userAnswersReader: Read[EquipmentsAndChargesDomain] = (
    equipmentsReader,
    chargesReader
  ).map(EquipmentsAndChargesDomain.apply)

  lazy val equipmentsReader: Read[Option[EquipmentsDomain]] =
    ContainerIndicatorPage.reader.apply(_).map(_.to(_.value)).flatMap {
      case ReaderSuccess(Some(true), pages) =>
        EquipmentsDomain.userAnswersReader.toOption.apply(pages)
      case ReaderSuccess(Some(false), pages) =>
        AddTransportEquipmentYesNoPage
          .filterOptionalDependent(identity) {
            EquipmentsDomain.userAnswersReader
          }
          .apply(pages)
      case ReaderSuccess(None, pages) =>
        UserAnswersReader.none.apply(pages)
    }

  lazy val chargesReader: Read[Option[PaymentMethod]] =
    SecurityDetailsTypePage.reader.apply(_).flatMap {
      case ReaderSuccess(NoSecurityDetails, pages) =>
        UserAnswersReader.none.apply(pages)
      case ReaderSuccess(_, pages) =>
        AddPaymentMethodYesNoPage.filterOptionalDependent(identity)(PaymentMethodPage.reader).apply(pages)
    }
}
