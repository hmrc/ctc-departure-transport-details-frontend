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

case class TransportMeansActiveDomain(
  identification: Identification,
  identificationNumber: String,
  nationality: Option[Nationality],
  customsOffice: CustomsOffice,
  conveyanceReferenceNumber: Option[String]
)(index: Index)
    extends JourneyDomainModel {

  def asString(implicit messages: Messages): String =
    TransportMeansActiveDomain.asString(identification, identificationNumber)

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] =
    if (TransportMeansActiveDomain.hasMultiplicity(userAnswers, phase)) {
      Some(activeRoutes.CheckYourAnswersController.onPageLoad(userAnswers.lrn, mode, index))
    } else {
      Some(transportMeansRoutes.TransportMeansCheckYourAnswersController.onPageLoad(userAnswers.lrn, mode))
    }
}

object TransportMeansActiveDomain {

  def asString(identification: Identification, identificationNumber: String)(implicit messages: Messages): String =
    s"${identification.asString} - $identificationNumber"

  def hasMultiplicity(userAnswers: UserAnswers, phase: Phase): Boolean = phase match {
    case Phase.PostTransition if userAnswers.get(OfficesOfTransitSection).isDefined => true
    case _                                                                          => false
  }

  implicit def userAnswersReader(index: Index): UserAnswersReader[TransportMeansActiveDomain] = {
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
    ).tupled.map((TransportMeansActiveDomain.apply _).tupled).map(_(index))
  }

}
