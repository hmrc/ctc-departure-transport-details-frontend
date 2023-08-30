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

import cats.implicits._
import config.PhaseConfig
import models.ProcedureType.Normal
import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.authorisationsAndLimit.authorisations.AuthorisationsAndLimitDomain
import models.journeyDomain.carrierDetails.CarrierDetailsDomain
import models.journeyDomain.equipment.EquipmentsAndChargesDomain
import models.journeyDomain.supplyChainActors.SupplyChainActorsDomain
import models.journeyDomain.transportMeans.TransportMeansDomain
import models.transportMeans.InlandMode
import models.transportMeans.InlandMode.Mail
import models.{Mode, Phase, UserAnswers}
import pages.authorisationsAndLimit.AuthorisationsInferredPage
import pages.authorisationsAndLimit.authorisations.AddAuthorisationsYesNoPage
import pages.carrierDetails.CarrierDetailYesNoPage
import pages.external.{ApprovedOperatorPage, ProcedureTypePage}
import pages.supplyChainActors.SupplyChainActorYesNoPage
import pages.transportMeans.{AddInlandModeYesNoPage, InlandModePage}
import play.api.mvc.Call

case class TransportDomain(
  preRequisites: PreRequisitesDomain,
  inlandMode: Option[InlandMode],
  transportMeans: Option[TransportMeansDomain],
  supplyChainActors: Option[SupplyChainActorsDomain],
  authorisationsAndLimit: Option[AuthorisationsAndLimitDomain],
  carrierDetails: Option[CarrierDetailsDomain],
  equipmentsAndCharges: EquipmentsAndChargesDomain
) extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] =
    Some(controllers.routes.TransportAnswersController.onPageLoad(userAnswers.lrn))
}

object TransportDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportDomain] = {

    implicit lazy val transportMeansReads: UserAnswersReader[Option[TransportMeansDomain]] =
      InlandModePage.optionalReader.flatMap {
        case Some(Mail) => none[TransportMeansDomain].pure[UserAnswersReader]
        case _          => UserAnswersReader[TransportMeansDomain].map(Some(_))
      }

    for {
      preRequisites          <- UserAnswersReader[PreRequisitesDomain]
      inlandMode             <- AddInlandModeYesNoPage.filterOptionalDependent(identity)(InlandModePage.reader)
      transportMeans         <- transportMeansReads
      supplyChainActors      <- SupplyChainActorYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[SupplyChainActorsDomain])
      authorisationsAndLimit <- authorisationsAndLimitReads
      carrierDetails         <- CarrierDetailYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[CarrierDetailsDomain])
      equipmentsAndCharges   <- UserAnswersReader[EquipmentsAndChargesDomain]
    } yield TransportDomain(
      preRequisites,
      inlandMode,
      transportMeans,
      supplyChainActors,
      authorisationsAndLimit,
      carrierDetails,
      equipmentsAndCharges
    )
  }

  implicit lazy val authorisationsAndLimitReads: UserAnswersReader[Option[AuthorisationsAndLimitDomain]] = {
    for {
      approvedOperator <- ApprovedOperatorPage.inferredReader
      procedureType    <- ProcedureTypePage.reader
      reader <- (approvedOperator, procedureType) match {
        case (false, Normal) =>
          AddAuthorisationsYesNoPage.filterOptionalDependent(identity)(UserAnswersReader[AuthorisationsAndLimitDomain])
        case _ =>
          AuthorisationsInferredPage.reader.flatMap {
            _ => UserAnswersReader[AuthorisationsAndLimitDomain].map(Some(_))
          }
      }
    } yield reader
  }
}
