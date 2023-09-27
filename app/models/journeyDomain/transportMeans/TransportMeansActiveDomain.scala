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
import config.Constants.{NoSecurityDetails, Rail}
import config.PhaseConfig
import controllers.transportMeans.active.{routes => activeRoutes}
import controllers.transportMeans.{routes => transportMeansRoutes}
import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.reference.transportMeans.active.Identification
import models.reference.{BorderMode, CustomsOffice, Nationality}
import models.{Index, Mode, Phase, UserAnswers}
import pages.external.SecurityDetailsTypePage
import pages.sections.external.OfficesOfTransitSection
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.active._
import play.api.i18n.Messages
import play.api.mvc.Call

sealed trait TransportMeansActiveDomain extends JourneyDomainModel

object TransportMeansActiveDomain {

  def hasMultiplicity(userAnswers: UserAnswers, phase: Phase): Boolean = phase match {
    case Phase.PostTransition => PostTransitionTransportMeansActiveDomain.hasMultiplicity(userAnswers)
    case Phase.Transition     => false
  }

  implicit def userAnswersReader(index: Index)(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportMeansActiveDomain] =
    phaseConfig.phase match {
      case Phase.Transition =>
        TransitionTransportMeansActiveDomain.userAnswersReader(index).widen[TransportMeansActiveDomain]
      case Phase.PostTransition =>
        PostTransitionTransportMeansActiveDomain.userAnswersReader(index).widen[TransportMeansActiveDomain]
    }

  def conveyanceReader(index: Index)(borderModeReader: => UserAnswersReader[Option[BorderMode]]): UserAnswersReader[Option[String]] =
    for {
      noSecurity    <- SecurityDetailsTypePage.reader.map(_ == NoSecurityDetails)
      airBorderMode <- borderModeReader.map(_.map(_.isAir))
      reader <- (noSecurity, airBorderMode) match {
        case (false, Some(true)) =>
          ConveyanceReferenceNumberPage(index).reader.map(Some(_))
        case _ =>
          ConveyanceReferenceNumberYesNoPage(index).filterOptionalDependent(identity)(ConveyanceReferenceNumberPage(index).reader)
      }
    } yield reader
}

case class TransitionTransportMeansActiveDomain(
  nationality: Option[Nationality],
  identification: Option[Identification],
  identificationNumber: Option[String],
  customsOffice: CustomsOffice,
  conveyanceReferenceNumber: Option[String]
) extends TransportMeansActiveDomain
    with JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] =
    Some(transportMeansRoutes.TransportMeansCheckYourAnswersController.onPageLoad(userAnswers.lrn, mode))
}

object TransitionTransportMeansActiveDomain {

  implicit def userAnswersReader(index: Index): UserAnswersReader[TransitionTransportMeansActiveDomain] =
    (
      nationalityReader(index),
      identificationReader(index),
      identificationNumberReader(index),
      CustomsOfficeActiveBorderPage(index).reader,
      conveyanceReader(index)
    ).tupled.map((TransitionTransportMeansActiveDomain.apply _).tupled)

  def nationalityReader(index: Index): UserAnswersReader[Option[Nationality]] =
    BorderModeOfTransportPage.optionalReader.map(_.map(_.code)).flatMap {
      case Some(Rail) =>
        AddNationalityYesNoPage(index).filterOptionalDependent(identity)(NationalityPage(index).reader)
      case _ =>
        NationalityPage(index).reader.map(Some(_))
    }

  def identificationReader(index: Index): UserAnswersReader[Option[Identification]] = {
    lazy val genericReader = InferredIdentificationPage(index).reader orElse IdentificationPage(index).reader
    for {
      borderMode                 <- BorderModeOfTransportPage.optionalReader
      registeredCountryIsPresent <- NationalityPage(index).optionalReader.map(_.isDefined)
      reader <-
        if (borderMode.exists(_.isRail) || registeredCountryIsPresent) {
          genericReader.map(Some(_))
        } else {
          AddIdentificationYesNoPage(index).filterOptionalDependent(identity)(genericReader)
        }
    } yield reader
  }

  def identificationNumberReader(index: Index): UserAnswersReader[Option[String]] =
    for {
      borderMode                 <- BorderModeOfTransportPage.optionalReader
      registeredCountryIsPresent <- NationalityPage(index).optionalReader.map(_.isDefined)
      reader <-
        if (borderMode.exists(_.isRail) || registeredCountryIsPresent) {
          IdentificationNumberPage(index).reader.map(Some(_))
        } else {
          AddVehicleIdentificationNumberYesNoPage(index).filterOptionalDependent(identity)(IdentificationNumberPage(index).reader)
        }
    } yield reader

  def conveyanceReader(index: Index): UserAnswersReader[Option[String]] =
    TransportMeansActiveDomain.conveyanceReader(index)(BorderModeOfTransportPage.optionalReader)
}

case class PostTransitionTransportMeansActiveDomain(
  identification: Identification,
  identificationNumber: String,
  nationality: Option[Nationality],
  customsOffice: CustomsOffice,
  conveyanceReferenceNumber: Option[String]
)(index: Index)
    extends TransportMeansActiveDomain
    with JourneyDomainModel {

  def asString(implicit messages: Messages): String =
    PostTransitionTransportMeansActiveDomain.asString(identification, identificationNumber)

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] =
    if (PostTransitionTransportMeansActiveDomain.hasMultiplicity(userAnswers)) {
      Some(activeRoutes.CheckYourAnswersController.onPageLoad(userAnswers.lrn, mode, index))
    } else {
      Some(transportMeansRoutes.TransportMeansCheckYourAnswersController.onPageLoad(userAnswers.lrn, mode))
    }
}

object PostTransitionTransportMeansActiveDomain {

  def asString(identification: Identification, identificationNumber: String)(implicit messages: Messages): String =
    s"${identification.asString} - $identificationNumber"

  def hasMultiplicity(userAnswers: UserAnswers): Boolean = userAnswers.get(OfficesOfTransitSection).isDefined

  implicit def userAnswersReader(index: Index): UserAnswersReader[PostTransitionTransportMeansActiveDomain] =
    (
      InferredIdentificationPage(index).reader orElse IdentificationPage(index).reader,
      IdentificationNumberPage(index).reader,
      AddNationalityYesNoPage(index).filterOptionalDependent(identity)(NationalityPage(index).reader),
      CustomsOfficeActiveBorderPage(index).reader,
      TransportMeansActiveDomain.conveyanceReader(index)(BorderModeOfTransportPage.reader.map(Some(_)))
    ).tupled.map((PostTransitionTransportMeansActiveDomain.apply _).tupled).map(_(index))

}
