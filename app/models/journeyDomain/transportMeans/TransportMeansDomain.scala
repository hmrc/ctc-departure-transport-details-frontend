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
import models.SecurityDetailsType.NoSecurityDetails
import models.domain._
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.transportMeans.{BorderModeOfTransport, InlandMode}
import models.{Mode, Phase, UserAnswers}
import pages.external.SecurityDetailsTypePage
import pages.preRequisites.ContainerIndicatorPage
import pages.transportMeans._
import play.api.mvc.Call

case class TransportMeansDomain(
  transportMeansDeparture: Option[TransportMeansDepartureDomain],
  borderModeOfTransport: Option[BorderModeOfTransport],
  transportMeansActiveList: Option[TransportMeansActiveListDomain]
) extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] =
    Option(routes.TransportMeansCheckYourAnswersController.onPageLoad(userAnswers.lrn, mode))
}

object TransportMeansDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportMeansDomain] =
    InlandModePage.reader.flatMap {
      case InlandMode.Mail =>
        UserAnswersReader(TransportMeansDomain(None, None, None))
      case _ =>
        (
          transportMeansDepartureReader,
          borderModeOfTransportReader,
          transportMeansActiveReader
        ).tupled.map((TransportMeansDomain.apply _).tupled)
    }

  def transportMeansDepartureReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[Option[TransportMeansDepartureDomain]] =
    phaseConfig.phase match {
      case Phase.Transition =>
        ContainerIndicatorPage.reader.flatMap {
          case true =>
            AddDepartureTransportMeansYesNoPage.filterOptionalDependent(identity) {
              UserAnswersReader[TransitionTransportMeansDepartureDomain].widen[TransportMeansDepartureDomain]
            }
          case false =>
            UserAnswersReader[TransitionTransportMeansDepartureDomain].widen[TransportMeansDepartureDomain].map(Some(_))
        }
      case Phase.PostTransition =>
        UserAnswersReader[PostTransitionTransportMeansDepartureDomain].map(Some(_))
    }

  // additional declaration type is part of pre-lodge so for time being always set to 'A'
  implicit val borderModeOfTransportReader: UserAnswersReader[Option[BorderModeOfTransport]] =
    SecurityDetailsTypePage.reader.flatMap {
      case NoSecurityDetails =>
        AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity)(BorderModeOfTransportPage.reader)
      case _ =>
        BorderModeOfTransportPage.reader.map(Some(_))
    }

  implicit val transportMeansActiveReader: UserAnswersReader[Option[TransportMeansActiveListDomain]] =
    SecurityDetailsTypePage.reader.flatMap {
      case NoSecurityDetails =>
        AddActiveBorderTransportMeansYesNoPage.filterOptionalDependent(identity) {
          UserAnswersReader[TransportMeansActiveListDomain]
        }
      case _ =>
        UserAnswersReader[TransportMeansActiveListDomain].map(Some(_))
    }
}
