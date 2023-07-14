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

import cats.implicits._
import config.PhaseConfig
import controllers.transportMeans.routes
import models.domain._
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.transportMeans.BorderModeOfTransport
import models.{Mode, Phase, UserAnswers}
import pages.preRequisites.ContainerIndicatorPage
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.departure.AddVehicleIdentificationYesNoPage
import play.api.mvc.Call

case class TransportMeansDomain(
  transportMeansDeparture: Option[TransportMeansDepartureDomain],
  borderModeOfTransport: BorderModeOfTransport,
  transportMeansActiveList: TransportMeansActiveListDomain
) extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    Option(routes.TransportMeansCheckYourAnswersController.onPageLoad(userAnswers.lrn, mode))
}

object TransportMeansDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportMeansDomain] =
    (
      transportMeansDepartureReader,
      BorderModeOfTransportPage.reader,
      UserAnswersReader[TransportMeansActiveListDomain]
    ).tupled.map((TransportMeansDomain.apply _).tupled)

  def transportMeansDepartureReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[Option[TransportMeansDepartureDomain]] =
    phaseConfig.phase match {
      case Phase.Transition =>
        ContainerIndicatorPage.reader.flatMap {
          case true =>
            AddVehicleIdentificationYesNoPage.filterOptionalDependent(identity) {
              UserAnswersReader[TransitionTransportMeansDepartureDomain].widen[TransportMeansDepartureDomain]
            }
          case false =>
            UserAnswersReader[TransitionTransportMeansDepartureDomain].widen[TransportMeansDepartureDomain].map(Some(_))
        }
      case Phase.PostTransition =>
        UserAnswersReader[PostTransitionTransportMeansDepartureDomain].map(Some(_))
    }
}
