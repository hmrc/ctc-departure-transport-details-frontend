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

package models.journeyDomain.equipment.seal

import controllers.equipment.index.seals.routes
import models.journeyDomain._
import models.journeyDomain.Stage._
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.{Index, Mode, Phase, UserAnswers}
import pages.equipment.index.seals.IdentificationNumberPage
import pages.sections.equipment.SealsSection
import play.api.mvc.Call

case class SealDomain(
  identificationNumber: String
)(equipmentIndex: Index, sealIndex: Index)
    extends JourneyDomainModel {

  override def toString: String = identificationNumber

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] =
    stage match {
      case AccessingJourney =>
        Some(routes.IdentificationNumberController.onPageLoad(userAnswers.lrn, mode, equipmentIndex, sealIndex))
      case CompletingJourney =>
        SealsSection(equipmentIndex).route(userAnswers, mode)
    }
}

object SealDomain {

  implicit def userAnswersReader(equipmentIndex: Index, sealIndex: Index): Read[SealDomain] =
    IdentificationNumberPage(equipmentIndex, sealIndex).reader.map(SealDomain(_)(equipmentIndex, sealIndex))
}
