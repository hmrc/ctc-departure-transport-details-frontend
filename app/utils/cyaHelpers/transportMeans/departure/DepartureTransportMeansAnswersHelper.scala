/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.cyaHelpers.transportMeans.departure

import config.{FrontendAppConfig, PhaseConfig}
import models.reference.Nationality
import models.reference.transportMeans.departure.Identification
import models.{Index, Mode, UserAnswers}
import pages.transportMeans.departure._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.cyaHelpers.AnswersHelper

class DepartureTransportMeansAnswersHelper(
  userAnswers: UserAnswers,
  mode: Mode,
  departureIndex: Index
)(implicit messages: Messages, appConfig: FrontendAppConfig, phaseConfig: PhaseConfig)
    extends AnswersHelper(userAnswers, mode) {

  def departureIdentificationType: Option[SummaryListRow] = getAnswerAndBuildRow[Identification](
    page = IdentificationPage(departureIndex),
    formatAnswer = formatDynamicEnumAsText(_),
    prefix = "transportMeans.departure.identification",
    id = Some("change-transport-means-departure-identification")
  )

  def departureIdentificationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = MeansIdentificationNumberPage(departureIndex),
    formatAnswer = formatAsText,
    prefix = "transportMeans.departure.meansIdentificationNumber",
    id = Some("change-transport-means-departure-identification-number")
  )

  def departureNationality: Option[SummaryListRow] = getAnswerAndBuildRow[Nationality](
    page = VehicleCountryPage(departureIndex),
    formatAnswer = _.toString.toText,
    prefix = "transportMeans.departure.vehicleCountry",
    id = Some("change-transport-means-departure-vehicle-nationality")
  )

  def departureAddTypeYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddIdentificationTypeYesNoPage(departureIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "transportMeans.departure.addIdentificationTypeYesNo",
    id = Some("change-transport-means-departure-add-identification-type")
  )

  def departureAddIdentificationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddIdentificationNumberYesNoPage(departureIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "transportMeans.departure.addIdentificationNumberYesNo",
    id = Some("change-transport-means-departure-add-identification-number")
  )

  def departureAddNationality: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddVehicleCountryYesNoPage(departureIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "transportMeans.departure.addVehicleCountryYesNo",
    id = Some("change-transport-means-departure-add-nationality")
  )
}
