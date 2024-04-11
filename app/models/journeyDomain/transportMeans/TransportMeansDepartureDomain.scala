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
import models.reference.transportMeans.departure.Identification
import models.reference.{InlandMode, Nationality}
import models.{Index, OptionalBoolean, Phase}
import pages.preRequisites.ContainerIndicatorPage
import pages.transportMeans.departure._
import pages.transportMeans.InlandModePage
import play.api.i18n.Messages

sealed trait TransportMeansDepartureDomain extends JourneyDomainModel {
  val index: Index

  def asString(implicit messages: Messages): String
}

object TransportMeansDepartureDomain {

  implicit def userAnswersReader(index: Index)(implicit phaseConfig: PhaseConfig): Read[TransportMeansDepartureDomain] =
    phaseConfig.phase match {
      case Phase.Transition =>
        TransitionTransportMeansDepartureDomain.userAnswersReader(index)
      case Phase.PostTransition =>
        PostTransitionTransportMeansDepartureDomain.userAnswersReader(index)
    }
}

case class PostTransitionTransportMeansDepartureDomain(
  identification: Option[Identification],
  identificationNumber: String,
  nationality: Option[Nationality]
)(override val index: Index)
    extends TransportMeansDepartureDomain {

  override def asString(implicit messages: Messages): String =
    PostTransitionTransportMeansDepartureDomain.asString(identification, identificationNumber)
}

object PostTransitionTransportMeansDepartureDomain {

  def asString(identification: Option[Identification], identificationNumber: String)(implicit messages: Messages): String =
    identification.fold(identificationNumber)(
      value => s"${value.asString} - $identificationNumber"
    )

  implicit def userAnswersReader(index: Index): Read[TransportMeansDepartureDomain] =
    (
      AddIdentificationTypeYesNoPage(index).filterOptionalDependent(identity)(IdentificationPage(index).reader),
      MeansIdentificationNumberPage(index).reader,
      AddVehicleCountryYesNoPage(index).filterOptionalDependent(identity)(VehicleCountryPage(index).reader)
    ).map(PostTransitionTransportMeansDepartureDomain.apply(_, _, _)(index))
}

case class TransitionTransportMeansDepartureDomain(
  identification: Option[Identification],
  identificationNumber: Option[String],
  nationality: Option[Nationality]
)(override val index: Index)
    extends TransportMeansDepartureDomain {

  override def asString(implicit messages: Messages): String = this.toString
}

object TransitionTransportMeansDepartureDomain {

  implicit def userAnswersReader(index: Index): Read[TransportMeansDepartureDomain] = {
    lazy val identificationReader: Read[Option[Identification]] =
      AddDepartureTransportMeansYesNoPage(index).optionalReader.to {
        case Some(_) =>
          ContainerIndicatorPage.optionalReader.to {
            case Some(OptionalBoolean.no) =>
              IdentificationPage(index).reader.toOption
            case _ =>
              AddIdentificationTypeYesNoPage(index).filterOptionalDependent(identity)(IdentificationPage(index).reader)
          }
        case None =>
          IdentificationPage(index).reader.toOption
      }

    lazy val identificationNumberReader: Read[Option[String]] =
      ContainerIndicatorPage.optionalReader.to {
        case Some(OptionalBoolean.no) =>
          MeansIdentificationNumberPage(index).reader.toOption
        case _ =>
          AddIdentificationNumberYesNoPage(index).filterOptionalDependent(identity)(MeansIdentificationNumberPage(index).reader)
      }

    lazy val nationalityReader: Read[Option[Nationality]] =
      InlandModePage.optionalReader.to {
        case Some(InlandMode(Rail, _)) =>
          UserAnswersReader.none
        case _ =>
          ContainerIndicatorPage.optionalReader.to {
            case Some(OptionalBoolean.yes) =>
              AddVehicleCountryYesNoPage(index).filterOptionalDependent(identity)(VehicleCountryPage(index).reader)
            case _ =>
              VehicleCountryPage(index).reader.toOption
          }
      }

    (
      identificationReader,
      identificationNumberReader,
      nationalityReader
    ).map(TransitionTransportMeansDepartureDomain.apply(_, _, _)(index))
  }
}
