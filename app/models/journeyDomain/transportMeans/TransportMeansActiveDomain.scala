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
import models.domain._
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.reference.transportMeans.active.Identification
import models.reference.{BorderMode, CustomsOffice, Nationality}
import models.{Index, Mode, Phase, UserAnswers}
import pages.external.SecurityDetailsTypePage
import pages.sections.Section
import pages.sections.external.OfficesOfTransitSection
import pages.sections.transportMeans.{ActiveSection, TransportMeansSection}
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.active._
import play.api.i18n.Messages
import play.api.mvc.Call

sealed trait TransportMeansActiveDomain extends JourneyDomainModel {

  val index: Index

  def asString(implicit messages: Messages): String

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] =
    page(userAnswers) match {
      case Some(value) => value.route(userAnswers, mode)
      case None        => TransportMeansSection.route(userAnswers, mode)
    }

  override def page(userAnswers: UserAnswers): Option[Section[_]] = Some(ActiveSection(index))
}

object TransportMeansActiveDomain {

  def hasMultiplicity(userAnswers: UserAnswers)(implicit phaseConfig: PhaseConfig): Boolean =
    phaseConfig.phase match {
      case Phase.PostTransition => PostTransitionTransportMeansActiveDomain.hasMultiplicity(userAnswers)
      case Phase.Transition     => false
    }

  implicit def userAnswersReader(index: Index)(implicit phaseConfig: PhaseConfig): Read[TransportMeansActiveDomain] =
    phaseConfig.phase match {
      case Phase.Transition =>
        TransitionTransportMeansActiveDomain.userAnswersReader(index)
      case Phase.PostTransition =>
        PostTransitionTransportMeansActiveDomain.userAnswersReader(index)
    }

  def conveyanceReader(index: Index): Read[Option[String]] =
    (
      SecurityDetailsTypePage.reader.apply(_: Pages).map(_.to(_ == NoSecurityDetails)),
      BorderModeOfTransportPage.optionalReader.apply(_: Pages).map(_.to(_.exists(_.isAir)))
    ).to {
      case (false, true) =>
        ConveyanceReferenceNumberPage(index).reader.toOption
      case _ =>
        ConveyanceReferenceNumberYesNoPage(index).filterOptionalDependent(identity)(ConveyanceReferenceNumberPage(index).reader)
    }
}

case class TransitionTransportMeansActiveDomain(
  nationality: Option[Nationality],
  identification: Option[Identification],
  identificationNumber: Option[String],
  customsOffice: CustomsOffice,
  conveyanceReferenceNumber: Option[String]
)(override val index: Index)
    extends TransportMeansActiveDomain {

  override def asString(implicit messages: Messages): String = this.toString

  override def page(userAnswers: UserAnswers): Option[Section[_]] = None
}

object TransitionTransportMeansActiveDomain {

  implicit def userAnswersReader(index: Index): Read[TransportMeansActiveDomain] =
    (
      nationalityReader(index),
      identificationReader(index),
      identificationNumberReader(index),
      CustomsOfficeActiveBorderPage(index).reader,
      TransportMeansActiveDomain.conveyanceReader(index)
    ).map(TransitionTransportMeansActiveDomain.apply(_, _, _, _, _)(index))

  def nationalityReader(index: Index): Read[Option[Nationality]] =
    BorderModeOfTransportPage.optionalReader.to {
      case Some(BorderMode(Rail, _)) =>
        AddNationalityYesNoPage(index).filterOptionalDependent(identity)(NationalityPage(index).reader)
      case _ =>
        NationalityPage(index).reader.toOption
    }

  def identificationReader(index: Index): Read[Option[Identification]] = {
    lazy val genericReader = UserAnswersReader.readInferred(IdentificationPage(index), InferredIdentificationPage(index))
    (
      BorderModeOfTransportPage.optionalReader,
      NationalityPage(index).optionalReader.apply(_: Pages).map(_.to(_.isDefined))
    ).to {
      case (borderMode, registeredCountryIsPresent) =>
        if (borderMode.exists(_.isRail) || registeredCountryIsPresent) {
          genericReader.toOption
        } else {
          AddIdentificationYesNoPage(index).filterOptionalDependent(identity)(genericReader)
        }
    }
  }

  def identificationNumberReader(index: Index): Read[Option[String]] =
    (
      BorderModeOfTransportPage.optionalReader,
      NationalityPage(index).optionalReader.apply(_: Pages).map(_.to(_.isDefined))
    ).to {
      case (borderMode, registeredCountryIsPresent) =>
        if (borderMode.exists(_.isRail) || registeredCountryIsPresent) {
          IdentificationNumberPage(index).reader.toOption
        } else {
          AddVehicleIdentificationNumberYesNoPage(index).filterOptionalDependent(identity)(IdentificationNumberPage(index).reader)
        }
    }
}

case class PostTransitionTransportMeansActiveDomain(
  identification: Identification,
  identificationNumber: String,
  nationality: Option[Nationality],
  customsOffice: CustomsOffice,
  conveyanceReferenceNumber: Option[String]
)(override val index: Index)
    extends TransportMeansActiveDomain {

  override def asString(implicit messages: Messages): String =
    PostTransitionTransportMeansActiveDomain.asString(identification, identificationNumber)

  override def page(userAnswers: UserAnswers): Option[Section[_]] =
    if (PostTransitionTransportMeansActiveDomain.hasMultiplicity(userAnswers)) {
      super.page(userAnswers)
    } else {
      None
    }
}

object PostTransitionTransportMeansActiveDomain {

  def asString(identification: Identification, identificationNumber: String)(implicit messages: Messages): String =
    s"${identification.asString} - $identificationNumber"

  def hasMultiplicity(userAnswers: UserAnswers): Boolean =
    userAnswers.get(OfficesOfTransitSection).isDefined

  implicit def userAnswersReader(index: Index): Read[TransportMeansActiveDomain] =
    (
      UserAnswersReader.readInferred(IdentificationPage(index), InferredIdentificationPage(index)),
      IdentificationNumberPage(index).reader,
      AddNationalityYesNoPage(index).filterOptionalDependent(identity)(NationalityPage(index).reader),
      CustomsOfficeActiveBorderPage(index).reader,
      TransportMeansActiveDomain.conveyanceReader(index)
    ).map(PostTransitionTransportMeansActiveDomain.apply(_, _, _, _, _)(index))
}
