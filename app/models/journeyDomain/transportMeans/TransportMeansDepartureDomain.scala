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
import config.PhaseConfig
import models.Phase
import models.domain._
import models.journeyDomain.{JourneyDomainModel, ReaderSuccess}
import models.reference.Nationality
import models.reference.transportMeans.departure.Identification
import pages.preRequisites.ContainerIndicatorPage
import pages.transportMeans.departure._
import pages.transportMeans.{AddDepartureTransportMeansYesNoPage, InlandModePage}

sealed trait TransportMeansDepartureDomain extends JourneyDomainModel

object TransportMeansDepartureDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[TransportMeansDepartureDomain] =
    phaseConfig.phase match {
      case Phase.Transition =>
        TransitionTransportMeansDepartureDomain.userAnswersReader
      case Phase.PostTransition =>
        PostTransitionTransportMeansDepartureDomain.userAnswersReader
    }
}

case class PostTransitionTransportMeansDepartureDomain(
  identification: Option[Identification],
  identificationNumber: String,
  nationality: Option[Nationality]
) extends TransportMeansDepartureDomain

object PostTransitionTransportMeansDepartureDomain {

  implicit val userAnswersReader: Read[TransportMeansDepartureDomain] =
    (
      AddIdentificationTypeYesNoPage.filterOptionalDependent(identity)(IdentificationPage.reader),
      MeansIdentificationNumberPage.reader,
      AddVehicleCountryYesNoPage.filterOptionalDependent(identity)(VehicleCountryPage.reader)
    ).map(PostTransitionTransportMeansDepartureDomain.apply)
}

case class TransitionTransportMeansDepartureDomain(
  identification: Option[Identification],
  identificationNumber: Option[String],
  nationality: Option[Nationality]
) extends TransportMeansDepartureDomain

object TransitionTransportMeansDepartureDomain {

  implicit val userAnswersReader: Read[TransportMeansDepartureDomain] = {
    lazy val identificationReader: Read[Option[Identification]] =
      AddDepartureTransportMeansYesNoPage.optionalReader.apply(_).flatMap {
        case ReaderSuccess(Some(_), pages) =>
          ContainerIndicatorPage.reader.apply(pages).map(_.to(_.value)).flatMap {
            case ReaderSuccess(Some(false), pages) =>
              IdentificationPage.reader.toOption.apply(pages)
            case ReaderSuccess(_, pages) =>
              AddIdentificationTypeYesNoPage.filterOptionalDependent(identity)(IdentificationPage.reader).apply(pages)
          }
        case ReaderSuccess(None, pages) =>
          IdentificationPage.reader.toOption.apply(pages)
      }

    lazy val identificationNumberReader: Read[Option[String]] =
      ContainerIndicatorPage.reader.apply(_).map(_.to(_.value)).flatMap {
        case ReaderSuccess(Some(false), pages) =>
          MeansIdentificationNumberPage.reader.toOption.apply(pages)
        case ReaderSuccess(_, pages) =>
          AddIdentificationNumberYesNoPage.filterOptionalDependent(identity)(MeansIdentificationNumberPage.reader).apply(pages)
      }

    lazy val nationalityReader: Read[Option[Nationality]] =
      InlandModePage.optionalReader.apply(_).map(_.to(_.map(_.code))).flatMap {
        case ReaderSuccess(Some(Rail), pages) =>
          UserAnswersReader.none.apply(pages)
        case ReaderSuccess(_, pages) =>
          ContainerIndicatorPage.reader.apply(pages).map(_.to(_.value)).flatMap {
            case ReaderSuccess(Some(true), pages) =>
              AddVehicleCountryYesNoPage.filterOptionalDependent(identity)(VehicleCountryPage.reader).apply(pages)
            case ReaderSuccess(_, pages) =>
              VehicleCountryPage.reader.toOption.apply(pages)
          }
      }

    (
      identificationReader,
      identificationNumberReader,
      nationalityReader
    ).map(TransitionTransportMeansDepartureDomain.apply)
  }
}
