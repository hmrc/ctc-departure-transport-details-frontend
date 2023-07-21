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

import cats.data.Kleisli
import cats.implicits._
import config.PhaseConfig
import controllers.transportMeans.routes
import models.Phase.{PostTransition, Transition}
import models.SecurityDetailsType.NoSecurityDetails
import models.domain._
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.transportMeans.BorderModeOfTransport
import models.{Mode, Phase, UserAnswers}
import pages.external.{OfficeOfDepartureInCL010Page, SecurityDetailsTypePage}
import pages.preRequisites.ContainerIndicatorPage
import pages.transportMeans.departure.AddVehicleIdentificationYesNoPage
import pages.transportMeans.{AddBorderModeOfTransportYesNoPage, BorderModeOfTransportPage}
import play.api.mvc.Call

case class TransportMeansDomain(
  transportMeansDeparture: Option[TransportMeansDepartureDomain],
  borderModeOfTransport: Option[BorderModeOfTransport],
  transportMeansActiveList: TransportMeansActiveListDomain
) extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] =
    Option(routes.TransportMeansCheckYourAnswersController.onPageLoad(userAnswers.lrn, mode))
}

object TransportMeansDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportMeansDomain] =
    (
      transportMeansDepartureReader,
      borderModeOfTransportReader,
      transportMeansActiveReader
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

  // additional declaration type is part of pre-lodge so for time being always set to 'A'
  def borderModeOfTransportReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[Option[BorderModeOfTransport]] =
    phaseConfig.phase match {
      case Transition     => transitionModeOfTransportReader
      case PostTransition => borderModeOfTransportReader
    }

  def borderModeOfTransportOptionalityReader: UserAnswersReader[Option[BorderModeOfTransport]] =
    SecurityDetailsTypePage.reader flatMap {
      case NoSecurityDetails => AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity)(BorderModeOfTransportPage.reader)
      case _                 => BorderModeOfTransportPage.reader.map(Some(_))
    }

  def transitionModeOfTransportReader: UserAnswersReader[Option[BorderModeOfTransport]] =
    for {
      isOfficeOfDepartureInCL010 <- OfficeOfDepartureInCL010Page.reader
      result <- isOfficeOfDepartureInCL010 match {
        case true  => AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity)(BorderModeOfTransportPage.reader)
        case false => borderModeOfTransportOptionalityReader
      }
    } yield result

  implicit val transportMeansActiveReader: UserAnswersReader[TransportMeansActiveListDomain] =
    TransportMeansActiveListDomain.userAnswersReader

}

object TransportMeansTransitionDomain {

  implicit val userAnswersReader: UserAnswersReader[TransportMeansDomain] =
    (
      transportMeansDepartureReader,
      borderModeOfTransportReader,
      TransportMeansActiveListDomain.userAnswersReader

    ).tupled.map((TransportMeansDomain.apply _).tupled)

  def transportMeansDepartureReader(): UserAnswersReader[Option[TransportMeansDepartureDomain]] = {
    ContainerIndicatorPage.reader.flatMap {
      case true =>
        AddVehicleIdentificationYesNoPage.filterOptionalDependent(identity) {
          UserAnswersReader[TransitionTransportMeansDepartureDomain].widen[TransportMeansDepartureDomain]
        }
      case false =>
        UserAnswersReader[TransitionTransportMeansDepartureDomain].widen[TransportMeansDepartureDomain].map(Some(_))
    }
  }

  def borderModeOfTransportReader: UserAnswersReader[Option[BorderModeOfTransport]] = {
    for {
      isOfficeOfDepartureInCL010 <- OfficeOfDepartureInCL010Page.reader
      result <- isOfficeOfDepartureInCL010 match {
        case true => AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity)(BorderModeOfTransportPage.reader)
        case false => borderModeOfTransportOptionalityReader
      }
    } yield result
  }

  def borderModeOfTransportOptionalityReader: UserAnswersReader[Option[BorderModeOfTransport]] =
    SecurityDetailsTypePage.reader flatMap {
      case NoSecurityDetails => AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity)(BorderModeOfTransportPage.reader)
      case _ => BorderModeOfTransportPage.reader.map(Some(_))
    }
}

object TransportMeansPostTransitionDomain {

  implicit val userAnswersReader: UserAnswersReader[TransportMeansDomain] =
    (
      UserAnswersReader[PostTransitionTransportMeansDepartureDomain].map(Some(_)),
      borderModeOfTransportOptionalityReader,
      TransportMeansActiveListDomain.userAnswersReader

    ).tupled.map((TransportMeansDomain.apply _).tupled)

  def borderModeOfTransportOptionalityReader: UserAnswersReader[Option[BorderModeOfTransport]] =
    SecurityDetailsTypePage.reader flatMap {
      case NoSecurityDetails => AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity)(BorderModeOfTransportPage.reader)
      case _ => BorderModeOfTransportPage.reader.map(Some(_))
    }

}

