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

import config.Constants.ModeOfTransport.Rail
import config.Constants.SecurityType.NoSecurityDetails
import config.PhaseConfig
import models.journeyDomain._
import models.journeyDomain.JourneyDomainModel
import models.journeyDomain.transportMeans.PostTransitionTransportMeansDomain.{borderModeOfTransportReader, transportMeansActiveReader}
import models.reference.BorderMode
import models.{Index, OptionalBoolean, Phase, UserAnswers}
import pages.external.{OfficeOfDepartureInCL010Page, SecurityDetailsTypePage}
import pages.preRequisites.ContainerIndicatorPage
import pages.sections.Section
import pages.sections.transportMeans.TransportMeansSection
import pages.transportMeans._

sealed trait TransportMeansDomain extends JourneyDomainModel {

  override def page(userAnswers: UserAnswers): Option[Section[_]] = Some(TransportMeansSection)
}

object TransportMeansDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[TransportMeansDomain] =
    phaseConfig.phase match {
      case Phase.Transition =>
        TransitionTransportMeansDomain.userAnswersReader
      case Phase.PostTransition =>
        UserAnswersReader
          .success {
            ua => PostTransitionTransportMeansActiveDomain.hasMultiplicity(ua)
          }
          .to {
            case true =>
              PostTransitionTransportMeansMultipleActiveDomain.userAnswersReader
            case false =>
              PostTransitionTransportMeansSingleActiveDomain.userAnswersReader
          }
    }
}

case class TransitionTransportMeansDomain(
  transportMeansDeparture: Option[TransportMeansDepartureDomain],
  borderModeOfTransport: Option[BorderMode],
  transportMeansActiveList: Option[TransportMeansActiveDomain]
) extends TransportMeansDomain

object TransitionTransportMeansDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[TransportMeansDomain] = {

    lazy val transportMeansDepartureReader: Read[Option[TransportMeansDepartureDomain]] =
      ContainerIndicatorPage.optionalReader.to {
        case Some(OptionalBoolean.yes) =>
          AddDepartureTransportMeansYesNoPage
            .filterOptionalDependent(identity) {
              TransportMeansDepartureDomain.userAnswersReader
            }
        case _ =>
          TransportMeansDepartureDomain.userAnswersReader.toOption
      }

    lazy val borderModeOfTransportReader: Read[Option[BorderMode]] = {
      lazy val optionalReader: Read[Option[BorderMode]] = AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity) {
        BorderModeOfTransportPage.reader
      }

      OfficeOfDepartureInCL010Page.reader.to {
        case true =>
          optionalReader
        case false =>
          SecurityDetailsTypePage.reader.to {
            case NoSecurityDetails => optionalReader
            case _                 => BorderModeOfTransportPage.reader.toOption
          }
      }
    }

    lazy val transportMeansActiveReader: Read[Option[TransportMeansActiveDomain]] =
      BorderModeOfTransportPage.optionalReader.to {
        case Some(borderModeOfTransport) if borderModeOfTransport.code != Rail =>
          TransportMeansActiveDomain.userAnswersReader(Index(0)).toOption
        case _ =>
          AddActiveBorderTransportMeansYesNoPage
            .filterOptionalDependent(identity) {
              TransportMeansActiveDomain.userAnswersReader(Index(0))
            }
      }

    (
      transportMeansDepartureReader,
      borderModeOfTransportReader,
      transportMeansActiveReader
    ).map(TransitionTransportMeansDomain.apply)
  }
}

sealed trait PostTransitionTransportMeansDomain extends TransportMeansDomain

object PostTransitionTransportMeansDomain {

  lazy val borderModeOfTransportReader: Read[Option[BorderMode]] =
    SecurityDetailsTypePage.reader.to {
      case NoSecurityDetails =>
        AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity)(BorderModeOfTransportPage.reader)
      case _ =>
        BorderModeOfTransportPage.reader.toOption
    }

  def transportMeansActiveReader[T](read: Read[T]): Read[Option[T]] =
    SecurityDetailsTypePage.reader.to {
      case NoSecurityDetails => AddActiveBorderTransportMeansYesNoPage.filterOptionalDependent(identity)(read)
      case _                 => read.toOption
    }
}

case class PostTransitionTransportMeansMultipleActiveDomain(
  transportMeansDeparture: TransportMeansDepartureDomain,
  borderModeOfTransport: Option[BorderMode],
  transportMeansActiveList: Option[TransportMeansActiveListDomain]
) extends PostTransitionTransportMeansDomain

object PostTransitionTransportMeansMultipleActiveDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[TransportMeansDomain] =
    (
      TransportMeansDepartureDomain.userAnswersReader,
      borderModeOfTransportReader,
      transportMeansActiveReader(TransportMeansActiveListDomain.userAnswersReader)
    ).map(PostTransitionTransportMeansMultipleActiveDomain.apply)
}

case class PostTransitionTransportMeansSingleActiveDomain(
  transportMeansDeparture: TransportMeansDepartureDomain,
  borderModeOfTransport: Option[BorderMode],
  transportMeansActiveList: Option[TransportMeansActiveDomain]
) extends PostTransitionTransportMeansDomain

object PostTransitionTransportMeansSingleActiveDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[TransportMeansDomain] =
    (
      TransportMeansDepartureDomain.userAnswersReader,
      borderModeOfTransportReader,
      transportMeansActiveReader(TransportMeansActiveDomain.userAnswersReader(Index(0)))
    ).map(PostTransitionTransportMeansSingleActiveDomain.apply)
}
