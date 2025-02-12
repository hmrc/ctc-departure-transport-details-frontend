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

import config.Constants.SecurityType.NoSecurityDetails
import models.journeyDomain.*
import models.reference.transportMeans.active.Identification
import models.reference.{BorderMode, CustomsOffice, Nationality}
import models.{Index, Mode, UserAnswers}
import pages.external.SecurityDetailsTypePage
import pages.sections.Section
import pages.sections.external.OfficesOfTransitSection
import pages.sections.transportMeans.{ActiveSection, TransportMeansSection}
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.active.*
import play.api.i18n.Messages
import play.api.mvc.Call

case class TransportMeansActiveDomain(
  identification: Identification,
  identificationNumber: String,
  nationality: Nationality,
  customsOffice: CustomsOffice,
  conveyanceReferenceNumber: Option[String]
)(val index: Index)
    extends JourneyDomainModel {

  def asString(implicit messages: Messages): String =
    TransportMeansActiveDomain.asString(identification, identificationNumber)

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    page(userAnswers) match {
      case Some(value) => value.route(userAnswers, mode)
      case None        => TransportMeansSection.route(userAnswers, mode)
    }

  override def page(userAnswers: UserAnswers): Option[Section[?]] = Option.when(TransportMeansActiveDomain.hasMultiplicity(userAnswers))(ActiveSection(index))
}

object TransportMeansActiveDomain {

  def asString(identification: Identification, identificationNumber: String)(implicit messages: Messages): String =
    s"${identification.asString} - $identificationNumber"

  def hasMultiplicity(userAnswers: UserAnswers): Boolean =
    userAnswers.get(OfficesOfTransitSection).isDefined

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

  implicit def userAnswersReader(index: Index): Read[TransportMeansActiveDomain] =
    (
      UserAnswersReader.readInferred(IdentificationPage(index), InferredIdentificationPage(index)),
      IdentificationNumberPage(index).reader,
      NationalityPage(index).reader,
      CustomsOfficeActiveBorderPage(index).reader,
      conveyanceReader(index)
    ).map(TransportMeansActiveDomain.apply(_, _, _, _, _)(index))
}
