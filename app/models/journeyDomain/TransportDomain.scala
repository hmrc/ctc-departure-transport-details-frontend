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

import config.PhaseConfig
import models.domain.{GettableAsFilterForNextReaderOps, UserAnswersReader}
import models.journeyDomain.authorisationsAndLimit.authorisations.AuthorisationsAndLimitDomain
import models.journeyDomain.carrierDetails.CarrierDetailsDomain
import models.journeyDomain.equipment.EquipmentsAndChargesDomain
import models.journeyDomain.supplyChainActors.SupplyChainActorsDomain
import models.journeyDomain.transportMeans.TransportMeansDomain
import models.{Mode, UserAnswers}
import pages.authorisationsAndLimit.authorisations.AddAuthorisationsYesNoPage
import pages.carrierDetails.CarrierDetailYesNoPage
import pages.external.ApprovedOperatorPage
import pages.supplyChainActors.SupplyChainActorYesNoPage
import play.api.mvc.Call

case class TransportDomain(
  preRequisites: PreRequisitesDomain,
  transportMeans: TransportMeansDomain,
  supplyChainActors: Option[SupplyChainActorsDomain],
  authorisationsAndLimit: Option[AuthorisationsAndLimitDomain],
  carrierDetails: Option[CarrierDetailsDomain],
  equipmentsAndCharges: EquipmentsAndChargesDomain
) extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    Some(controllers.routes.TransportAnswersController.onPageLoad(userAnswers.lrn))
}

object TransportDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportDomain] = {

    implicit lazy val authorisationsAndLimitReads: UserAnswersReader[Option[AuthorisationsAndLimitDomain]] =
      ApprovedOperatorPage.inferredReader.flatMap {
        case true  => UserAnswersReader[AuthorisationsAndLimitDomain].map(Some(_))
        case false => AddAuthorisationsYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[AuthorisationsAndLimitDomain])
      }

    for {
      preRequisites          <- UserAnswersReader[PreRequisitesDomain]
      transportMeans         <- UserAnswersReader[TransportMeansDomain]
      supplyChainActors      <- SupplyChainActorYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[SupplyChainActorsDomain])
      authorisationsAndLimit <- authorisationsAndLimitReads
      carrierDetails         <- CarrierDetailYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[CarrierDetailsDomain])
      equipmentsAndCharges   <- UserAnswersReader[EquipmentsAndChargesDomain]
    } yield TransportDomain(preRequisites, transportMeans, supplyChainActors, authorisationsAndLimit, carrierDetails, equipmentsAndCharges)
  }

}
