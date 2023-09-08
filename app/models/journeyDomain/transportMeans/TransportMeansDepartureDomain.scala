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
import models.Phase
import models.domain._
import models.journeyDomain.JourneyDomainModel
import models.reference.Nationality
import models.transportMeans.departure.Identification
import pages.preRequisites.ContainerIndicatorPage
import pages.transportMeans.InlandModePage
import pages.transportMeans.departure._

sealed trait TransportMeansDepartureDomain extends JourneyDomainModel

object TransportMeansDepartureDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportMeansDepartureDomain] =
    phaseConfig.phase match {
      case Phase.Transition =>
        TransitionTransportMeansDepartureDomain.userAnswersReader.widen[TransportMeansDepartureDomain]
      case Phase.PostTransition =>
        PostTransitionTransportMeansDepartureDomain.userAnswersReader.widen[TransportMeansDepartureDomain]
    }
}

case class PostTransitionTransportMeansDepartureDomain(
  identification: Option[Identification],
  identificationNumber: String,
  nationality: Option[Nationality]
) extends TransportMeansDepartureDomain

object PostTransitionTransportMeansDepartureDomain {

  implicit val userAnswersReader: UserAnswersReader[PostTransitionTransportMeansDepartureDomain] =
    (
      AddIdentificationTypeYesNoPage.filterOptionalDependent(identity)(IdentificationPage.reader),
      MeansIdentificationNumberPage.reader,
      AddVehicleCountryYesNoPage.filterOptionalDependent(identity)(VehicleCountryPage.reader)
    ).tupled.map((PostTransitionTransportMeansDepartureDomain.apply _).tupled)
}

case class TransitionTransportMeansDepartureDomain(
  identification: Option[Identification],
  identificationNumber: Option[String],
  nationality: Option[Nationality]
) extends TransportMeansDepartureDomain

object TransitionTransportMeansDepartureDomain {

  implicit val userAnswersReader: UserAnswersReader[TransitionTransportMeansDepartureDomain] = {
    val identificationReader: UserAnswersReader[Option[Identification]] =
      ContainerIndicatorPage.reader.flatMap {
        case true  => AddIdentificationTypeYesNoPage.filterOptionalDependent(identity)(IdentificationPage.reader)
        case false => IdentificationPage.reader.map(Some(_))
      }

    val identificationNumberReader: UserAnswersReader[Option[String]] =
      ContainerIndicatorPage.reader.flatMap {
        case true  => AddIdentificationNumberYesNoPage.filterOptionalDependent(identity)(MeansIdentificationNumberPage.reader)
        case false => MeansIdentificationNumberPage.reader.map(Some(_))
      }

    val nationalityReader: UserAnswersReader[Option[Nationality]] =
      InlandModePage.optionalReader.flatMap {
        case Some(inlandMode) if inlandMode.isRail =>
          none[Nationality].pure[UserAnswersReader]
        case _ =>
          ContainerIndicatorPage.reader.flatMap {
            case true  => AddVehicleCountryYesNoPage.filterOptionalDependent(identity)(VehicleCountryPage.reader)
            case false => VehicleCountryPage.reader.map(Some(_))
          }
      }

    (
      identificationReader,
      identificationNumberReader,
      nationalityReader
    ).tupled.map((TransitionTransportMeansDepartureDomain.apply _).tupled)
  }
}
