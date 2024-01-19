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
import config.Constants.ModeOfTransport.Rail
import config.Constants.SecurityType.NoSecurityDetails
import config.PhaseConfig
import models.Phase
import models.domain._
import models.journeyDomain.{JourneyDomainModel, ReaderSuccess}
import models.reference.BorderMode
import pages.external.{OfficeOfDepartureInCL010Page, SecurityDetailsTypePage}
import pages.preRequisites.ContainerIndicatorPage
import pages.sections.Section
import pages.sections.transportMeans.TransportMeansSection
import pages.transportMeans._

sealed trait TransportMeansDomain extends JourneyDomainModel {

  override def page: Option[Section[_]] = Some(TransportMeansSection)
}

object TransportMeansDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[TransportMeansDomain] =
    phaseConfig.phase match {
      case Phase.Transition =>
        TransitionTransportMeansDomain.userAnswersReader
      case Phase.PostTransition =>
        PostTransitionTransportMeansDomain.userAnswersReader
    }

}

case class TransitionTransportMeansDomain(
  transportMeansDeparture: Option[TransportMeansDepartureDomain],
  borderModeOfTransport: Option[BorderMode],
  transportMeansActiveList: Option[TransportMeansActiveListDomain]
) extends TransportMeansDomain

object TransitionTransportMeansDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[TransportMeansDomain] = {

    lazy val transportMeansDepartureReader: Read[Option[TransportMeansDepartureDomain]] =
      ContainerIndicatorPage.reader.apply(_).map(_.to(_.value)).flatMap {
        case ReaderSuccess(Some(true), pages) =>
          AddDepartureTransportMeansYesNoPage
            .filterOptionalDependent(identity) {
              TransportMeansDepartureDomain.userAnswersReader
            }
            .apply(pages)
        case ReaderSuccess(_, pages) =>
          TransportMeansDepartureDomain.userAnswersReader.toOption.apply(pages)
      }

    lazy val borderModeOfTransportReader: Read[Option[BorderMode]] = {
      lazy val optionalReader: Read[Option[BorderMode]] = AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity) {
        BorderModeOfTransportPage.reader
      }

      OfficeOfDepartureInCL010Page.reader.apply(_).flatMap {
        case ReaderSuccess(true, pages) =>
          optionalReader.apply(pages)
        case ReaderSuccess(false, pages) =>
          SecurityDetailsTypePage.reader.apply(pages).flatMap {
            case ReaderSuccess(NoSecurityDetails, pages) => optionalReader.apply(pages)
            case ReaderSuccess(_, pages)                 => BorderModeOfTransportPage.reader.toOption.apply(pages)
          }
      }
    }

    lazy val transportMeansActiveReader: Read[Option[TransportMeansActiveListDomain]] =
      BorderModeOfTransportPage.optionalReader.apply(_).flatMap {
        case ReaderSuccess(Some(borderModeOfTransport), pages) if borderModeOfTransport.code != Rail =>
          TransportMeansActiveListDomain.userAnswersReader.toOption.apply(pages)
        case ReaderSuccess(_, pages) =>
          AddActiveBorderTransportMeansYesNoPage
            .filterOptionalDependent(identity) {
              TransportMeansActiveListDomain.userAnswersReader
            }
            .apply(pages)
      }

    (
      transportMeansDepartureReader,
      borderModeOfTransportReader,
      transportMeansActiveReader
    ).map(TransitionTransportMeansDomain.apply)
  }
}

case class PostTransitionTransportMeansDomain(
  transportMeansDeparture: TransportMeansDepartureDomain,
  borderModeOfTransport: Option[BorderMode],
  transportMeansActiveList: Option[TransportMeansActiveListDomain]
) extends TransportMeansDomain

object PostTransitionTransportMeansDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[TransportMeansDomain] = {

    lazy val borderModeOfTransportReader: Read[Option[BorderMode]] =
      SecurityDetailsTypePage.reader.apply(_).flatMap {
        case ReaderSuccess(NoSecurityDetails, pages) =>
          AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity)(BorderModeOfTransportPage.reader).apply(pages)
        case ReaderSuccess(_, pages) =>
          BorderModeOfTransportPage.reader.toOption.apply(pages)
      }

    lazy val transportMeansActiveReader: Read[Option[TransportMeansActiveListDomain]] =
      SecurityDetailsTypePage.reader.apply(_).flatMap {
        case ReaderSuccess(NoSecurityDetails, pages) =>
          AddActiveBorderTransportMeansYesNoPage
            .filterOptionalDependent(identity) {
              TransportMeansActiveListDomain.userAnswersReader
            }
            .apply(pages)
        case ReaderSuccess(_, pages) =>
          TransportMeansActiveListDomain.userAnswersReader.toOption.apply(pages)
      }

    (
      TransportMeansDepartureDomain.userAnswersReader,
      borderModeOfTransportReader,
      transportMeansActiveReader
    ).map(PostTransitionTransportMeansDomain.apply)
  }
}
