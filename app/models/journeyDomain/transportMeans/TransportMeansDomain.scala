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
import config.Constants.NoSecurityDetails
import config.PhaseConfig
import controllers.transportMeans.routes
import models.domain._
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.transportMeans.BorderModeOfTransport
import models.transportMeans.BorderModeOfTransport.ChannelTunnel
import models.{Mode, Phase, UserAnswers}
import pages.external.{OfficeOfDepartureInCL010Page, SecurityDetailsTypePage}
import pages.preRequisites.ContainerIndicatorPage
import pages.transportMeans._
import play.api.mvc.Call

sealed trait TransportMeansDomain extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] =
    Option(routes.TransportMeansCheckYourAnswersController.onPageLoad(userAnswers.lrn, mode))

}

object TransportMeansDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportMeansDomain] =
    phaseConfig.phase match {
      case Phase.Transition =>
        TransitionTransportMeansDomain.userAnswersReader.widen[TransportMeansDomain]
      case Phase.PostTransition =>
        PostTransitionTransportMeansDomain.userAnswersReader.widen[TransportMeansDomain]
    }

}

case class TransitionTransportMeansDomain(
  transportMeansDeparture: Option[TransportMeansDepartureDomain],
  borderModeOfTransport: Option[BorderModeOfTransport],
  transportMeansActiveList: Option[TransportMeansActiveListDomain]
) extends TransportMeansDomain

object TransitionTransportMeansDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransitionTransportMeansDomain] = {

    lazy val transportMeansDepartureReader: UserAnswersReader[Option[TransportMeansDepartureDomain]] =
      ContainerIndicatorPage.reader.map(_.value).flatMap {
        case Some(true) =>
          AddDepartureTransportMeansYesNoPage.filterOptionalDependent(identity) {
            TransportMeansDepartureDomain.userAnswersReader
          }
        case _ =>
          TransportMeansDepartureDomain.userAnswersReader.map(Some(_))
      }

    lazy val borderModeOfTransportReader: UserAnswersReader[Option[BorderModeOfTransport]] = {
      lazy val optionalReader: UserAnswersReader[Option[BorderModeOfTransport]] = AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity) {
        BorderModeOfTransportPage.reader
      }

      OfficeOfDepartureInCL010Page.reader.flatMap {
        case true =>
          optionalReader
        case false =>
          SecurityDetailsTypePage.reader flatMap {
            case NoSecurityDetails => optionalReader
            case _                 => BorderModeOfTransportPage.reader.map(Some(_))
          }
      }
    }

    lazy val transportMeansActiveReader: UserAnswersReader[Option[TransportMeansActiveListDomain]] =
      BorderModeOfTransportPage.optionalReader.flatMap {
        case Some(borderModeOfTransport) if borderModeOfTransport != ChannelTunnel =>
          UserAnswersReader[TransportMeansActiveListDomain].map(Some(_))
        case _ =>
          AddActiveBorderTransportMeansYesNoPage.filterOptionalDependent(identity) {
            UserAnswersReader[TransportMeansActiveListDomain]
          }
      }

    (
      transportMeansDepartureReader,
      borderModeOfTransportReader,
      transportMeansActiveReader
    ).tupled.map((TransitionTransportMeansDomain.apply _).tupled)
  }
}

case class PostTransitionTransportMeansDomain(
  transportMeansDeparture: TransportMeansDepartureDomain,
  borderModeOfTransport: Option[BorderModeOfTransport],
  transportMeansActiveList: Option[TransportMeansActiveListDomain]
) extends TransportMeansDomain

object PostTransitionTransportMeansDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[PostTransitionTransportMeansDomain] = {

    lazy val borderModeOfTransportReader: UserAnswersReader[Option[BorderModeOfTransport]] =
      SecurityDetailsTypePage.reader flatMap {
        case NoSecurityDetails => AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity)(BorderModeOfTransportPage.reader)
        case _                 => BorderModeOfTransportPage.reader.map(Some(_))
      }

    lazy val transportMeansActiveReader: UserAnswersReader[Option[TransportMeansActiveListDomain]] =
      SecurityDetailsTypePage.reader.flatMap {
        case NoSecurityDetails =>
          AddActiveBorderTransportMeansYesNoPage.filterOptionalDependent(identity) {
            UserAnswersReader[TransportMeansActiveListDomain]
          }
        case _ =>
          UserAnswersReader[TransportMeansActiveListDomain].map(Some(_))
      }

    (
      TransportMeansDepartureDomain.userAnswersReader,
      borderModeOfTransportReader,
      transportMeansActiveReader
    ).tupled.map((PostTransitionTransportMeansDomain.apply _).tupled)
  }
}
