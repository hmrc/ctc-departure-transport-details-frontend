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
import models.journeyDomain.{JourneyDomainModel, _}
import models.reference.transportMeans.departure.Identification
import models.reference.{InlandMode, Nationality}
import models.{Index, Mode, OptionalBoolean, Phase, UserAnswers}
import pages.preRequisites.ContainerIndicatorPage
import pages.sections.Section
import pages.sections.transportMeans.{DepartureSection, TransportMeansSection}
import pages.transportMeans.departure._
import pages.transportMeans.{AddDepartureTransportMeansYesNoPage, InlandModePage}
import play.api.i18n.Messages
import play.api.mvc.Call

sealed trait TransportMeansDepartureDomain extends JourneyDomainModel {
  val index: Index

  def asString(implicit messages: Messages): String

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] =
    page(userAnswers) match {
      case Some(value) => value.route(userAnswers, mode)
      case None        => TransportMeansSection.route(userAnswers, mode)
    }

  override def page(userAnswers: UserAnswers): Option[Section[_]] = Some(DepartureSection(index))

}

object TransportMeansDepartureDomain {

  implicit def userAnswersReader(index: Index)(implicit phaseConfig: PhaseConfig): Read[TransportMeansDepartureDomain] =
    phaseConfig.phase match {
      case Phase.Transition =>
        if (phaseConfig.areB1892AndB1897Disabled) {
          TransitionTransportMeansDepartureRulesDisabledDomain.userAnswersReader(index)
        } else
          TransitionTransportMeansDepartureDomain.userAnswersReader(index)
      case Phase.PostTransition =>
        PostTransitionTransportMeansDepartureDomain.userAnswersReader(index)
    }
}

case class PostTransitionTransportMeansDepartureDomain(
  identification: Identification,
  identificationNumber: String,
  nationality: Nationality
)(override val index: Index)
    extends TransportMeansDepartureDomain {

  override def asString(implicit messages: Messages): String =
    PostTransitionTransportMeansDepartureDomain.asString(identification, identificationNumber, index)
}

object PostTransitionTransportMeansDepartureDomain {

  def asString(identification: Identification, identificationNumber: String, index: Index)(implicit messages: Messages): String =
    messages("departureTransportMeans.label.bothArgs", index.display, identification.asString, identificationNumber)

  implicit def userAnswersReader(index: Index): Read[TransportMeansDepartureDomain] =
    (
      IdentificationPage(index).reader,
      MeansIdentificationNumberPage(index).reader,
      VehicleCountryPage(index).reader
    ).map(PostTransitionTransportMeansDepartureDomain.apply(_, _, _)(index))
}

case class TransitionTransportMeansDepartureDomain(
  identification: Option[Identification],
  identificationNumber: Option[String],
  nationality: Option[Nationality]
)(override val index: Index)
    extends TransportMeansDepartureDomain {

  override def asString(implicit messages: Messages): String =
    TransitionTransportMeansDepartureDomain.asString(identification, identificationNumber, index)
}

object TransitionTransportMeansDepartureDomain {

  def asString(identification: Option[Identification], identificationNumber: Option[String], index: Index)(implicit messages: Messages): String =
    (identification, identificationNumber) match {
      case (Some(id), Some(idNumber)) => messages("departureTransportMeans.label.bothArgs", index.display, id.asString, idNumber)
      case (Some(id), None)           => messages("departureTransportMeans.label.oneArg", index.display, id.asString)
      case (None, Some(idNumber))     => messages("departureTransportMeans.label.oneArg", index.display, idNumber)
      case _                          => messages("departureTransportMeans.label.noArgs", index.display)
    }

  implicit def userAnswersReader(index: Index): Read[TransportMeansDepartureDomain] = {
    lazy val identificationReader: Read[Option[Identification]] =
      AddDepartureTransportMeansYesNoPage.optionalReader.to {
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

case class TransitionTransportMeansDepartureRulesDisabledDomain(
  identification: Identification,
  identificationNumber: String,
  nationality: Nationality
)(override val index: Index)
    extends TransportMeansDepartureDomain {

  override def asString(implicit messages: Messages): String =
    TransitionTransportMeansDepartureRulesDisabledDomain.asString(identification, identificationNumber, index)
}

object TransitionTransportMeansDepartureRulesDisabledDomain {

  def asString(identification: Identification, identificationNumber: String, index: Index)(implicit messages: Messages): String =
    messages("departureTransportMeans.label.bothArgs", index.display, identification.asString, identificationNumber)

  implicit def userAnswersReader(index: Index): Read[TransportMeansDepartureDomain] =
    (
      IdentificationPage(index).reader,
      MeansIdentificationNumberPage(index).reader,
      VehicleCountryPage(index).reader
    ).map(TransitionTransportMeansDepartureRulesDisabledDomain.apply(_, _, _)(index))
}
