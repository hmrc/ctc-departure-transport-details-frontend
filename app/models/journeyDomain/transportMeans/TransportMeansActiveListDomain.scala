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

package models.journeyDomain.transportMeans

import config.PhaseConfig
import controllers.transportMeans.active.routes
import models.domain._
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.{Index, Mode, Phase, RichJsArray, UserAnswers}
import pages.sections.transportMeans.ActivesSection
import play.api.mvc.Call

case class TransportMeansActiveListDomain(
  transportMeansActiveListDomain: Seq[TransportMeansActiveDomain]
) extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] =
    Some(routes.AddAnotherBorderTransportController.onPageLoad(userAnswers.lrn, mode))
}

object TransportMeansActiveListDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[TransportMeansActiveListDomain] = {

    val activesReader: Read[Seq[TransportMeansActiveDomain]] =
      ActivesSection.arrayReader.to {
        case x if x.isEmpty =>
          TransportMeansActiveDomain.userAnswersReader(Index(0)).toSeq
        case x =>
          x.traverse[TransportMeansActiveDomain](TransportMeansActiveDomain.userAnswersReader(_).apply(_))
      }

    activesReader.map(TransportMeansActiveListDomain.apply)
  }
}
