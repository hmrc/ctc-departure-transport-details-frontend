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
import models.journeyDomain.transportMeans.{PostTransitionTransportMeansActiveDomain, TransportMeansActiveDomain, TransportMeansDepartureDomain}
import models.reference.{BorderMode, InlandMode}
import models.{Index, Mode, UserAnswers}
import pages.sections.transportMeans.{ActivesSection, DeparturesSection}
import pages.transportMeans._
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
    getAnswersAndBuildSectionRows(ActivesSection)(activeBorderTransportMeans)

  // only used in post-transition (no multiplicity during transition period)
  def activeBorderTransportMeans(index: Index): Option[SummaryListRow] = getAnswerAndBuildSectionRow[TransportMeansActiveDomain](
    formatAnswer = _.asString.toText,
    prefix = "transportMeans.active",
    id = Some(s"change-active-border-transport-means-${index.display}"),
    args = index.display
  )(PostTransitionTransportMeansActiveDomain.userAnswersReader(index).apply(Nil))

  def departureTransportsMeans: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(DeparturesSection)(departureTransportMeans)

  def departureTransportMeans(index: Index): Option[SummaryListRow] = getAnswerAndBuildSectionRow[TransportMeansDepartureDomain](
    formatAnswer = _.asString.toText,
    prefix = "transportMeans.departure",
    id = Some(s"change-departure-means-of-transport-${index.display}"),
    args = index.display
  )(TransportMeansDepartureDomain.userAnswersReader(index).apply(Nil))

  def addOrRemoveActiveBorderTransportsMeans(): Option[Link] = {
    import controllers.transportMeans.active.routes
    buildLink(ActivesSection) {
      Link(
        id = "add-or-remove-border-means-of-transport",
        text = messages("transportMeans.borderMeans.addOrRemove"),
        href = routes.AddAnotherBorderTransportController.onPageLoad(userAnswers.lrn, mode).url
      )
    }
  }

  def addOrRemoveDepartureTransportsMeans(): Option[Link] = {
    import controllers.transportMeans.departure.routes
    buildLink(DeparturesSection) {
      Link(
        id = "add-or-remove-departure-means-of-transport",
        text = messages("transportMeans.departureMeans.addOrRemove"),
        href = routes.AddAnotherDepartureTransportMeansController.onPageLoad(userAnswers.lrn, mode).url
      )
    }
  }

  def addModeCrossingBorder(): Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddBorderModeOfTransportYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transportMeans.addBorderModeOfTransportYesNo",
    id = Some("change-add-border-mode-of-transport")
  )

  def modeCrossingBorder: Option[SummaryListRow] = getAnswerAndBuildRow[BorderMode](
    page = BorderModeOfTransportPage,
    formatAnswer = formatDynamicEnumAsText(_),
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
    formatAnswer = formatDynamicEnumAsText(_),
    prefix = "transportMeans.inlandMode",
    id = Some("change-transport-means-inland-mode")
  )

  def addDepartureTransportMeans(): Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddDepartureTransportMeansYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transportMeans.addDepartureTransportMeansYesNo",
    id = Some("change-add-departure-transport-means")
  )

  def addActiveBorderTransportMeans: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddActiveBorderTransportMeansYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "transportMeans.addActiveBorderTransportMeansYesNo",
    id = Some("change-add-active-border-transport-means")
  )

}
