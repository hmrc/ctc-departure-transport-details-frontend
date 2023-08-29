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

import config.{FrontendAppConfig, PhaseConfig}
import controllers.transportMeans.active.routes
import models.journeyDomain.transportMeans.PostTransitionTransportMeansActiveDomain
import models.reference.Nationality
import models.transportMeans.departure.Identification
import models.transportMeans.{BorderModeOfTransport, InlandMode}
import models.{Index, Mode, UserAnswers}
import pages.sections.transportMeans.TransportMeansActiveListSection
import pages.transportMeans._
import pages.transportMeans.departure._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.cyaHelpers.AnswersHelper
import viewModels.Link

class TransportMeansCheckYourAnswersHelper(
  userAnswers: UserAnswers,
  mode: Mode
)(implicit messages: Messages, appConfig: FrontendAppConfig, phaseConfig: PhaseConfig)
    extends AnswersHelper(userAnswers, mode) {

  def activeBorderTransportsMeans: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(TransportMeansActiveListSection)(activeBorderTransportMeans)

  // only used in post-transition (no multiplicity during transition period)
  def activeBorderTransportMeans(index: Index): Option[SummaryListRow] = getAnswerAndBuildSectionRow[PostTransitionTransportMeansActiveDomain](
    formatAnswer = _.asString.toText,
    prefix = "transportMeans.active",
    id = Some(s"change-active-border-transport-means-${index.display}"),
    args = index.display
  )(PostTransitionTransportMeansActiveDomain.userAnswersReader(index))

  def addOrRemoveActiveBorderTransportsMeans(): Option[Link] = buildLink(TransportMeansActiveListSection) {
    Link(
      id = "add-or-remove-border-means-of-transport",
      text = messages("transportMeans.borderMeans.addOrRemove"),
      href = routes.AddAnotherBorderTransportController.onPageLoad(userAnswers.lrn, mode).url
    )
  }

  def addModeCrossingBorder(): Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddBorderModeOfTransportYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transportMeans.addBorderModeOfTransportYesNo",
    id = Some("change-add-border-mode-of-transport")
  )

  def modeCrossingBorder: Option[SummaryListRow] = getAnswerAndBuildRow[BorderModeOfTransport](
    page = BorderModeOfTransportPage,
    formatAnswer = formatEnumAsText(BorderModeOfTransport.messageKeyPrefix),
    prefix = "transportMeans.borderModeOfTransport",
    id = Some("change-border-mode-of-transport")
  )

  def addInlandModeYesNo: Option[SummaryListRow] = {
    val prefix = "transportMeans.addInlandModeYesNo"
    getAnswerAndBuildRow[Boolean](
      page = AddInlandModeYesNoPage,
      formatAnswer = formatAsYesOrNo(_, prefix),
      prefix = prefix,
      id = Some("change-add-transport-means-inland-mode")
    )
  }

  def inlandMode: Option[SummaryListRow] = getAnswerAndBuildRow[InlandMode](
    page = InlandModePage,
    formatAnswer = formatEnumAsText(InlandMode.messageKeyPrefix),
    prefix = "transportMeans.inlandMode",
    id = Some("change-transport-means-inland-mode")
  )

  def addDepartureTransportMeans: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddDepartureTransportMeansYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transportMeans.addDepartureTransportMeansYesNo",
    id = Some("change-add-departure-transport-means")
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

  def departureAddTypeYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddIdentificationTypeYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transportMeans.departure.addIdentificationTypeYesNo",
    id = Some("change-transport-means-departure-add-identification-type")
  )

  def departureAddIdentificationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddIdentificationNumberYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transportMeans.departure.addIdentificationNumberYesNo",
    id = Some("change-transport-means-departure-add-identification-number")
  )

  def departureAddNationality: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddVehicleCountryYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transportMeans.departure.addVehicleCountryYesNo",
    id = Some("change-transport-means-departure-add-nationality")
  )

  def addActiveBorderTransportMeans: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddActiveBorderTransportMeansYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transportMeans.addActiveBorderTransportMeansYesNo",
    id = Some("change-add-active-border-transport-means")
  )

}
