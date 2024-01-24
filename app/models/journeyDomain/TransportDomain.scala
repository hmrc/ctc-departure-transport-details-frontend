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

package models.journeyDomain

import config.Constants.ModeOfTransport.Mail
import config.PhaseConfig
import models.ProcedureType.Normal
import models.UserAnswers
import models.domain._
import models.journeyDomain.authorisationsAndLimit.authorisations.AuthorisationsAndLimitDomain
import models.journeyDomain.carrierDetails.CarrierDetailsDomain
import models.journeyDomain.equipment.EquipmentsAndChargesDomain
import models.journeyDomain.supplyChainActors.SupplyChainActorsDomain
import models.journeyDomain.transportMeans.TransportMeansDomain
import models.reference.InlandMode
import pages.authorisationsAndLimit.{AddAuthorisationsYesNoPage, AuthorisationsInferredPage}
import pages.carrierDetails.CarrierDetailYesNoPage
import pages.external.{ApprovedOperatorPage, ProcedureTypePage}
import pages.sections.{Section, TransportSection}
import pages.supplyChainActors.SupplyChainActorYesNoPage
import pages.transportMeans.{AddInlandModeYesNoPage, InlandModePage}

case class TransportDomain(
  preRequisites: PreRequisitesDomain,
  inlandMode: Option[InlandMode],
  transportMeans: Option[TransportMeansDomain],
  supplyChainActors: Option[SupplyChainActorsDomain],
  authorisationsAndLimit: Option[AuthorisationsAndLimitDomain],
  carrierDetails: Option[CarrierDetailsDomain],
  equipmentsAndCharges: EquipmentsAndChargesDomain
) extends JourneyDomainModel {

  override def page(userAnswers: UserAnswers): Option[Section[_]] = Some(TransportSection)
}

object TransportDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportDomain] = {

    implicit lazy val transportMeansReads: Read[Option[TransportMeansDomain]] =
      InlandModePage.optionalReader.to {
        case Some(InlandMode(Mail, _)) => UserAnswersReader.none
        case _                         => TransportMeansDomain.userAnswersReader.toOption
      }

    (
      PreRequisitesDomain.userAnswersReader,
      AddInlandModeYesNoPage.filterOptionalDependent(identity)(InlandModePage.reader),
      transportMeansReads,
      SupplyChainActorYesNoPage.filterOptionalDependent(identity)(SupplyChainActorsDomain.userAnswersReader),
      authorisationsAndLimitReads,
      CarrierDetailYesNoPage.filterOptionalDependent(identity)(CarrierDetailsDomain.userAnswersReader),
      EquipmentsAndChargesDomain.userAnswersReader
    ).map(TransportDomain.apply).apply(Nil)
  }

  implicit lazy val authorisationsAndLimitReads: Read[Option[AuthorisationsAndLimitDomain]] =
    (
      ApprovedOperatorPage.inferredReader,
      ProcedureTypePage.reader
    ).to {
      case (false, Normal) =>
        AddAuthorisationsYesNoPage.filterOptionalDependent(identity)(AuthorisationsAndLimitDomain.userAnswersReader)
      case _ =>
        AuthorisationsInferredPage.reader.to {
          _ => AuthorisationsAndLimitDomain.userAnswersReader.toOption
        }
    }
}
