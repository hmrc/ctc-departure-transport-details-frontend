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
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
import controllers.transportMeans.routes
import models.SecurityDetailsType.EntryAndExitSummaryDeclarationSecurityDetails
import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.transportMeans.BorderModeOfTransport
import models.transportMeans.departure.InlandMode
import models.{Mode, UserAnswers}
import pages.external.SecurityDetailsTypePage
import pages.transportMeans.departure.InlandModePage
import pages.transportMeans.{AddBorderModeOfTransportYesNoPage, BorderModeOfTransportPage}
import play.api.mvc.Call

sealed trait TransportMeansDomain extends JourneyDomainModel {
  val inlandMode: InlandMode

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    Option(routes.TransportMeansCheckYourAnswersController.onPageLoad(userAnswers.lrn, mode))
}

object TransportMeansDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportMeansDomain] =
    InlandModePage.reader.flatMap {
      case InlandMode.Mail =>
        UserAnswersReader(TransportMeansDomainWithMailInlandMode).widen[TransportMeansDomain]
      case x =>
        UserAnswersReader[TransportMeansDomainWithOtherInlandMode](
          TransportMeansDomainWithOtherInlandMode.userAnswersReader(x)
        ).widen[TransportMeansDomain]
    }
}

case object TransportMeansDomainWithMailInlandMode extends TransportMeansDomain {
  override val inlandMode: InlandMode = InlandMode.Mail
}

case class TransportMeansDomainWithOtherInlandMode(
  override val inlandMode: InlandMode,
  transportMeansDeparture: TransportMeansDepartureDomain,
  borderModeOfTransport: Option[BorderModeOfTransport],
=======
import models.domain.{GettableAsReaderOps, UserAnswersReader}
=======
import models.Phase
import models.domain._
>>>>>>> 9cdc46d... CTCP-3213: Departure means of transport transition nav.
import models.journeyDomain.JourneyDomainModel
=======
import controllers.transportMeans.routes
import models.domain._
import models.journeyDomain.{JourneyDomainModel, Stage}
>>>>>>> 8acc0d6... CTCP-3213: Updated UserAnswersEntryGenerators.
import models.transportMeans.BorderModeOfTransport
import models.{Mode, Phase, UserAnswers}
import pages.preRequisites.ContainerIndicatorPage
<<<<<<< HEAD
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.departure.AddVehicleIdentificationYesNoPage
=======
import pages.transportMeans.{AddBorderModeOfTransportYesNoPage, AddDepartureTransportMeansYesNoPage, BorderModeOfTransportPage}
>>>>>>> f5b23fa... CTCP-3434: Moved 'Add departure transport means?' page.
import play.api.mvc.Call

case class TransportMeansDomain(
  transportMeansDeparture: Option[TransportMeansDepartureDomain],
  borderModeOfTransport: BorderModeOfTransport,
>>>>>>> e6e6e14... CTCP-3213: Initial refactoring to move inland mode to parent domain.
  transportMeansActiveList: TransportMeansActiveListDomain
) extends JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] =
    Option(routes.TransportMeansCheckYourAnswersController.onPageLoad(userAnswers.lrn, mode))
}

object TransportMeansDomain {

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
  implicit def userAnswersReader(inlandMode: InlandMode)(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportMeansDomainWithOtherInlandMode] =
=======
  implicit val borderModeOfTransportReader: UserAnswersReader[Option[BorderModeOfTransport]] =
    // additional declaration type is part of pre-lodge so for time being always set to 'A'
    SecurityDetailsTypePage.reader.flatMap {
      case securityType if securityType != EntryAndExitSummaryDeclarationSecurityDetails =>
        BorderModeOfTransportPage.reader.map(Some(_))
      case _ =>
        AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity)(BorderModeOfTransportPage.reader)
    }

  implicit def userAnswersReader(inlandMode: InlandMode): UserAnswersReader[TransportMeansDomainWithOtherInlandMode] =
>>>>>>> de17bb0... CTCP-3463: Added AddBorderModeOfTransportYesNo to nav
=======
  implicit def userAnswersReader(inlandMode: InlandMode)(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportMeansDomainWithOtherInlandMode] =
>>>>>>> ead852e... CTCP-3213: Initial refactor.
    (
      UserAnswersReader(inlandMode),
      UserAnswersReader[TransportMeansDepartureDomain],
      borderModeOfTransportReader,
=======
  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportMeansDomain] =
    (
      transportMeansDepartureReader,
      BorderModeOfTransportPage.reader,
>>>>>>> e6e6e14... CTCP-3213: Initial refactoring to move inland mode to parent domain.
      UserAnswersReader[TransportMeansActiveListDomain]
    ).tupled.map((TransportMeansDomain.apply _).tupled)

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
<<<<<<< HEAD
=======

  // additional declaration type is part of pre-lodge so for time being always set to 'A'
  implicit val borderModeOfTransportReader: UserAnswersReader[Option[BorderModeOfTransport]] =
    SecurityDetailsTypePage.reader.flatMap {
      case NoSecurityDetails =>
        AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity)(BorderModeOfTransportPage.reader)
      case _ =>
        BorderModeOfTransportPage.reader.map(Some(_))
    }

  implicit val transportMeansActiveReader: UserAnswersReader[TransportMeansActiveListDomain] =
    TransportMeansActiveListDomain.userAnswersReader
>>>>>>> 1d4687d... Using phase in routeIfCompleted.
}
