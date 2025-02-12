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
import models.ProcedureType.{Normal, Simplified}
import models.UserAnswers
import models.journeyDomain.*
import models.journeyDomain.additionalInformation.AdditionalInformationsDomain
import models.journeyDomain.additionalReferences.AdditionalReferencesDomain
import models.journeyDomain.authorisationsAndLimit.authorisations.AuthorisationsAndLimitDomain
import models.journeyDomain.carrierDetails.CarrierDetailsDomain
import models.journeyDomain.equipment.EquipmentsAndChargesDomain
import models.journeyDomain.supplyChainActors.SupplyChainActorsDomain
import models.journeyDomain.transportMeans.TransportMeansDomain
import models.reference.InlandMode
import pages.additionalInformation.AddAdditionalInformationYesNoPage
import pages.additionalReference.AddAdditionalReferenceYesNoPage
import pages.authorisationsAndLimit.AuthorisationsInferredPage
import pages.carrierDetails.CarrierDetailYesNoPage
import pages.external.ProcedureTypePage
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
  equipmentsAndCharges: EquipmentsAndChargesDomain,
  additionalReferences: Option[AdditionalReferencesDomain],
  additionalInformations: Option[AdditionalInformationsDomain]
) extends JourneyDomainModel {

  override def page(userAnswers: UserAnswers): Option[Section[?]] = Some(TransportSection)
}

object TransportDomain {

  implicit val userAnswersReader: UserAnswersReader[TransportDomain] = {

    implicit lazy val transportMeansReads: Read[Option[TransportMeansDomain]] =
      InlandModePage.optionalReader.to {
        case Some(InlandMode(Mail, _)) => UserAnswersReader.none
        case _                         => TransportMeansDomain.userAnswersReader.toOption
      }

    implicit lazy val additionalReferencesReads: Read[Option[AdditionalReferencesDomain]] =
      AddAdditionalReferenceYesNoPage.filterOptionalDependent(identity)(AdditionalReferencesDomain.userAnswersReader)

    implicit lazy val additionalInformationsReads: Read[Option[AdditionalInformationsDomain]] =
      AddAdditionalInformationYesNoPage.filterOptionalDependent(identity)(AdditionalInformationsDomain.userAnswersReader)

    (
      PreRequisitesDomain.userAnswersReader,
      AddInlandModeYesNoPage.filterOptionalDependent(identity)(InlandModePage.reader),
      transportMeansReads,
      SupplyChainActorYesNoPage.filterOptionalDependent(identity)(SupplyChainActorsDomain.userAnswersReader),
      authorisationsAndLimitReads,
      CarrierDetailYesNoPage.filterOptionalDependent(identity)(CarrierDetailsDomain.userAnswersReader),
      EquipmentsAndChargesDomain.userAnswersReader,
      additionalReferencesReads,
      additionalInformationsReads
    ).map(TransportDomain.apply).apply(Nil)
  }

  implicit lazy val authorisationsAndLimitReads: Read[Option[AuthorisationsAndLimitDomain]] =
    ProcedureTypePage.reader.to {
      case Normal =>
        UserAnswersReader.none
      case Simplified =>
        AuthorisationsInferredPage.reader.to {
          _ => AuthorisationsAndLimitDomain.userAnswersReader.toOption
        }
    }
}
