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
import controllers.transportMeans.active.{routes => activeRoutes}
import controllers.transportMeans.{routes => transportMeansRoutes}
import models.SecurityDetailsType.NoSecurityDetails
import models.domain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.reference.{CustomsOffice, Nationality}
import models.transportMeans.BorderModeOfTransport._
import models.transportMeans.active.Identification
import models.{Index, Mode, Phase, UserAnswers}
import pages.external.SecurityDetailsTypePage
import pages.sections.external.OfficesOfTransitSection
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.active._
import play.api.i18n.Messages
import play.api.mvc.Call

sealed trait TransportMeansActiveDomain extends JourneyDomainModel {

  val identification: Identification
  val identificationNumber: String

  def asString(implicit messages: Messages): String =
    TransportMeansActiveDomain.asString(identification, identificationNumber)

}

object TransportMeansActiveDomain {

  def asString(identification: Identification, identificationNumber: String)(implicit messages: Messages): String =
    s"${identification.asString} - $identificationNumber"

  implicit def userAnswersReader(index: Index)(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportMeansActiveDomain] =
    phaseConfig.phase match {
      case Phase.Transition =>
        TransitionTransportMeansActiveDomain.userAnswersReader(index).widen[TransportMeansActiveDomain]
      case Phase.PostTransition =>
        PostTransitionTransportMeansActiveDomain.userAnswersReader(index).widen[TransportMeansActiveDomain]
    }

}

case class TransitionTransportMeansActiveDomain(
  nationality: Option[Nationality],
  identification: Identification,
  identificationNumber: String,
  customsOffice: CustomsOffice,
  conveyanceReferenceNumber: Option[String]
) extends TransportMeansActiveDomain
    with JourneyDomainModel {

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] =
    Some(transportMeansRoutes.TransportMeansCheckYourAnswersController.onPageLoad(userAnswers.lrn, mode))
}

object TransitionTransportMeansActiveDomain {

  implicit def userAnswersReader(index: Index): UserAnswersReader[TransitionTransportMeansActiveDomain] = {
    lazy val conveyanceReads: UserAnswersReader[Option[String]] =
      for {
        securityDetails <- SecurityDetailsTypePage.reader
        borderMode      <- BorderModeOfTransportPage.reader
        reader <- (securityDetails, borderMode) match {
          case (x, Air) if x != NoSecurityDetails =>
            ConveyanceReferenceNumberPage(index).reader.map(Some(_))
          case _ =>
            ConveyanceReferenceNumberYesNoPage(index).filterOptionalDependent(identity)(ConveyanceReferenceNumberPage(index).reader)
        }
      } yield reader

    lazy val nationalityReader: UserAnswersReader[Option[Nationality]] =
      for {
        borderMode <- BorderModeOfTransportPage.reader
        reader <- borderMode match {
          case ChannelTunnel =>
            AddNationalityYesNoPage(index).filterOptionalDependent(identity)(NationalityPage(index).reader)
          case _ =>
            NationalityPage(index).reader.map(Some(_))
        }
      } yield reader
    (
      nationalityReader,
      InferredIdentificationPage(index).reader orElse IdentificationPage(index).reader,
      IdentificationNumberPage(index).reader,
      CustomsOfficeActiveBorderPage(index).reader,
      conveyanceReads
    ).tupled.map((TransitionTransportMeansActiveDomain.apply _).tupled)
  }
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

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] =
    if (PostTransitionTransportMeansActiveDomain.hasMultiplicity(userAnswers)) {
      Some(activeRoutes.CheckYourAnswersController.onPageLoad(userAnswers.lrn, mode, index))
    } else {
      Some(transportMeansRoutes.TransportMeansCheckYourAnswersController.onPageLoad(userAnswers.lrn, mode))
    }
}

object PostTransitionTransportMeansActiveDomain {

  def hasMultiplicity(userAnswers: UserAnswers): Boolean = userAnswers.get(OfficesOfTransitSection).isDefined

  implicit def userAnswersReader(index: Index): UserAnswersReader[PostTransitionTransportMeansActiveDomain] = {
    lazy val conveyanceReads: UserAnswersReader[Option[String]] =
      for {
        securityDetails <- SecurityDetailsTypePage.reader
        borderMode      <- BorderModeOfTransportPage.reader
        reader <- (securityDetails, borderMode) match {
          case (x, Air) if x != NoSecurityDetails =>
            ConveyanceReferenceNumberPage(index).reader.map(Some(_))
          case _ =>
            ConveyanceReferenceNumberYesNoPage(index).filterOptionalDependent(identity)(ConveyanceReferenceNumberPage(index).reader)
        }
      } yield reader

    (
      InferredIdentificationPage(index).reader orElse IdentificationPage(index).reader,
      IdentificationNumberPage(index).reader,
      AddNationalityYesNoPage(index).filterOptionalDependent(identity)(NationalityPage(index).reader),
      CustomsOfficeActiveBorderPage(index).reader,
      conveyanceReads
    ).tupled.map((PostTransitionTransportMeansActiveDomain.apply _).tupled).map(_(index))
  }

}
