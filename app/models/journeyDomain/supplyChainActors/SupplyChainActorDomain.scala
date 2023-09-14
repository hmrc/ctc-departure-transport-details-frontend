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

package models.journeyDomain.supplyChainActors

import cats.implicits.catsSyntaxTuple2Semigroupal
import models.domain.{GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.Stage.{AccessingJourney, CompletingJourney}
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.reference.supplyChainActors.SupplyChainActorType
import models.{Index, Mode, Phase, UserAnswers}
import pages.supplyChainActors.index.{IdentificationNumberPage, SupplyChainActorTypePage}
import play.api.mvc.Call

case class SupplyChainActorDomain(role: SupplyChainActorType, identification: String)(index: Index) extends JourneyDomainModel {

  def asString: String =
    SupplyChainActorDomain.asString(role, identification)

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] = Some {
    stage match {
      case AccessingJourney =>
        controllers.supplyChainActors.index.routes.SupplyChainActorTypeController.onPageLoad(userAnswers.lrn, mode, index)
      case CompletingJourney =>
        controllers.supplyChainActors.routes.AddAnotherSupplyChainActorController.onPageLoad(userAnswers.lrn, mode)
    }
  }
}

object SupplyChainActorDomain {

  def asString(role: SupplyChainActorType, identification: String): String =
    s"${role.toString} - $identification"

  def userAnswersReader(index: Index): UserAnswersReader[SupplyChainActorDomain] =
    (
      SupplyChainActorTypePage(index).reader,
      IdentificationNumberPage(index).reader
    ).mapN {
      (actor, identification) => SupplyChainActorDomain(actor, identification)(index)
    }

}
