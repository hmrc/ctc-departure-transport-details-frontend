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

import models.journeyDomain.{JourneyDomainModel, *}
import models.reference.Nationality
import models.reference.transportMeans.departure.Identification
import models.{Index, Mode, TransportMeans, UserAnswers}
import pages.sections.Section
import pages.sections.transportMeans.{DepartureSection, TransportMeansSection}
import pages.transportMeans.departure.*
import play.api.i18n.Messages
import play.api.mvc.Call

case class TransportMeansDepartureDomain(
  identification: Identification,
  identificationNumber: String,
  nationality: Nationality
)(val index: Index)
    extends JourneyDomainModel {

  def asString(implicit messages: Messages): String =
    TransportMeansDepartureDomain.asString(identification, identificationNumber, index)

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    page(userAnswers) match {
      case Some(value) => value.route(userAnswers, mode)
      case None        => TransportMeansSection.route(userAnswers, mode)
    }

  override def page(userAnswers: UserAnswers): Option[Section[?]] = Some(DepartureSection(index))
}

object TransportMeansDepartureDomain {

  def asString(identification: Identification, identificationNumber: String, index: Index)(implicit messages: Messages): String =
    TransportMeans(index, Some(identification), Some(identificationNumber)).forAddAnotherDisplay

  implicit def userAnswersReader(index: Index): Read[TransportMeansDepartureDomain] =
    (
      IdentificationPage(index).reader,
      MeansIdentificationNumberPage(index).reader,
      VehicleCountryPage(index).reader
    ).map(TransportMeansDepartureDomain.apply(_, _, _)(index))
}
