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

package utils.cyaHelpers.transportMeans

import config.FrontendAppConfig
import controllers.transportMeans.active.routes
import models.journeyDomain.transportMeans.TransportMeansActiveDomain
import models.reference.Nationality
import models.transportMeans.BorderModeOfTransport
import models.transportMeans.departure.{Identification, InlandMode}
import models.{Index, Mode, UserAnswers}
import pages.sections.transportMeans.TransportMeansActiveListSection
import pages.transportMeans._
import pages.transportMeans.departure._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.cyaHelpers.AnswersHelper
import viewModels.Link

class TransportMeansCheckYourAnswersHelper(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages, config: FrontendAppConfig)
    extends AnswersHelper(userAnswers, mode) {

  def activeBorderTransportsMeans: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(TransportMeansActiveListSection)(activeBorderTransportMeans)

  def activeBorderTransportMeans(index: Index): Option[SummaryListRow] = getAnswerAndBuildSectionRow[TransportMeansActiveDomain](
    formatAnswer = _.asString.toText,
    prefix = "transportMeans.active",
    id = Some(s"change-active-border-transport-means-${index.display}"),
    args = index.display
  )(TransportMeansActiveDomain.userAnswersReader(index))

  def addOrRemoveActiveBorderTransportsMeans(): Option[Link] = buildLink(TransportMeansActiveListSection) {
    Link(
      id = "add-or-remove-border-means-of-transport",
      text = messages("transportMeans.borderMeans.addOrRemove"),
      href = routes.AddAnotherBorderTransportController.onPageLoad(userAnswers.lrn, mode).url
    )
  }

  def anotherVehicleCrossing: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AnotherVehicleCrossingYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transportMeans.anotherVehicleCrossingYesNo",
    id = Some("change-another-vehicle-crossing-border")
  )

  def modeCrossingBorder: Option[SummaryListRow] = getAnswerAndBuildRow[BorderModeOfTransport](
    page = BorderModeOfTransportPage,
    formatAnswer = formatEnumAsText(BorderModeOfTransport.messageKeyPrefix),
    prefix = "transportMeans.borderModeOfTransport",
    id = Some("change-border-mode-of-transport")
  )

  def inlandMode: Option[SummaryListRow] = getAnswerAndBuildRow[InlandMode](
    page = InlandModePage,
    formatAnswer = formatEnumAsText(InlandMode.messageKeyPrefix),
    prefix = "transportMeans.departure.inlandMode",
    id = Some("change-transport-means-inland-mode")
  )

  def departureIdentificationType: Option[SummaryListRow] = getAnswerAndBuildRow[Identification](
    page = IdentificationPage,
    formatAnswer = formatEnumAsText(Identification.messageKeyPrefix),
    prefix = "transportMeans.departure.identification",
    id = Some("change-transport-means-departure-identification")
  )

  def departureIdentificationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = MeansIdentificationNumberPage,
    formatAnswer = formatAsText,
    prefix = "transportMeans.departure.meansIdentificationNumber",
    id = Some("change-transport-means-departure-identification-number")
  )

  def departureNationality: Option[SummaryListRow] = getAnswerAndBuildRow[Nationality](
    page = VehicleCountryPage,
    formatAnswer = formatAsText,
    prefix = "transportMeans.departure.vehicleCountry",
    id = Some("change-transport-means-departure-vehicle-nationality")
  )

}
