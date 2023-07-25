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
import models.transportMeans.BorderModeOfTransport.ChannelTunnel
import models.{Mode, Phase, UserAnswers}
import pages.external.{OfficeOfDepartureInCL010Page, SecurityDetailsTypePage}
import pages.preRequisites.ContainerIndicatorPage
import pages.transportMeans.{
  AddActiveBorderTransportMeansYesNoPage,
  AddBorderModeOfTransportYesNoPage,
  AddDepartureTransportMeansYesNoPage,
  BorderModeOfTransportPage,
  InlandModePage
}
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

    val transportMeansDepartureReader: UserAnswersReader[Option[TransportMeansDepartureDomain]] =
      ContainerIndicatorPage.reader.flatMap {
        case true =>
          AddDepartureTransportMeansYesNoPage.filterOptionalDependent(identity) {
            TransportMeansDepartureDomain.userAnswersReader
          }
        case false =>
          TransportMeansDepartureDomain.userAnswersReader.map(Some(_))
      }

    def borderModeOfTransportOptionalityReader: UserAnswersReader[Option[BorderModeOfTransport]] =
      SecurityDetailsTypePage.reader flatMap {
        case NoSecurityDetails => AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity)(BorderModeOfTransportPage.reader)
        case _                 => BorderModeOfTransportPage.reader.map(Some(_))
      }

    val borderModeOfTransportReader: UserAnswersReader[Option[BorderModeOfTransport]] = {
      for {
        isOfficeOfDepartureInCL010 <- OfficeOfDepartureInCL010Page.reader
        result <-
          if (isOfficeOfDepartureInCL010) {
            AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity)(BorderModeOfTransportPage.reader)
          } else {
            borderModeOfTransportOptionalityReader
          }
      } yield result
    }

    val transportMeansActiveReader: UserAnswersReader[Option[TransportMeansActiveListDomain]] =
      BorderModeOfTransportPage.optionalReader.flatMap {
        case Some(borderModeOfTransport) if borderModeOfTransport != ChannelTunnel =>
          UserAnswersReader[TransportMeansActiveListDomain].map(Some(_))
        case _ =>
          AddActiveBorderTransportMeansYesNoPage.filterOptionalDependent(identity) {
            UserAnswersReader[TransportMeansActiveListDomain]
          }
      }

    InlandModePage.reader.flatMap {
      case InlandMode.Mail => UserAnswersReader(TransitionTransportMeansDomain(None, None, None))
      case _ =>
        (
          transportMeansDepartureReader,
          borderModeOfTransportReader,
          transportMeansActiveReader
        ).tupled.map((TransitionTransportMeansDomain.apply _).tupled)
    }
  }
}

case class PostTransitionTransportMeansDomain(
  transportMeansDeparture: Option[TransportMeansDepartureDomain],
  borderModeOfTransport: Option[BorderModeOfTransport],
  transportMeansActiveList: Option[TransportMeansActiveListDomain]
) extends TransportMeansDomain

object PostTransitionTransportMeansDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[PostTransitionTransportMeansDomain] = {

    val borderModeOfTransportOptionalityReader: UserAnswersReader[Option[BorderModeOfTransport]] =
      SecurityDetailsTypePage.reader flatMap {
        case NoSecurityDetails => AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity)(BorderModeOfTransportPage.reader)
        case _                 => BorderModeOfTransportPage.reader.map(Some(_))
      }

    val transportMeansActiveReader: UserAnswersReader[Option[TransportMeansActiveListDomain]] =
      SecurityDetailsTypePage.reader.flatMap {
        case NoSecurityDetails =>
          AddActiveBorderTransportMeansYesNoPage.filterOptionalDependent(identity) {
            UserAnswersReader[TransportMeansActiveListDomain]
          }
        case _ =>
          UserAnswersReader[TransportMeansActiveListDomain].map(Some(_))
      }
    InlandModePage.reader.flatMap {
      case InlandMode.Mail => UserAnswersReader(PostTransitionTransportMeansDomain(None, None, None))
      case _ =>
        (
          TransportMeansDepartureDomain.userAnswersReader.map(Some(_)),
          borderModeOfTransportOptionalityReader,
          transportMeansActiveReader
        ).tupled.map((PostTransitionTransportMeansDomain.apply _).tupled)
    }
  }
}
