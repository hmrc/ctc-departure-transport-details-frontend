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
import config.PhaseConfig
import models.journeyDomain._
import models.journeyDomain.JourneyDomainModel
import models.reference.transportMeans.departure.Identification
import models.reference.{InlandMode, Nationality}
import models.{OptionalBoolean, Phase}
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
      AddDepartureTransportMeansYesNoPage.optionalReader.to {
        case Some(_) =>
          ContainerIndicatorPage.optionalReader.to {
            case Some(OptionalBoolean.no) =>
              IdentificationPage.reader.toOption
            case _ =>
              AddIdentificationTypeYesNoPage.filterOptionalDependent(identity)(IdentificationPage.reader)
          }
        case None =>
          IdentificationPage.reader.toOption
      }

    lazy val identificationNumberReader: Read[Option[String]] =
      ContainerIndicatorPage.optionalReader.to {
        case Some(OptionalBoolean.no) =>
          MeansIdentificationNumberPage.reader.toOption
        case _ =>
          AddIdentificationNumberYesNoPage.filterOptionalDependent(identity)(MeansIdentificationNumberPage.reader)
      }

    lazy val nationalityReader: Read[Option[Nationality]] =
      InlandModePage.optionalReader.to {
        case Some(InlandMode(Rail, _)) =>
          UserAnswersReader.none
        case _ =>
          ContainerIndicatorPage.optionalReader.to {
            case Some(OptionalBoolean.yes) =>
              AddVehicleCountryYesNoPage.filterOptionalDependent(identity)(VehicleCountryPage.reader)
            case _ =>
              VehicleCountryPage.reader.toOption
          }
      }

    (
      identificationReader,
      identificationNumberReader,
      nationalityReader
    ).map(TransitionTransportMeansDepartureDomain.apply)
  }
}
