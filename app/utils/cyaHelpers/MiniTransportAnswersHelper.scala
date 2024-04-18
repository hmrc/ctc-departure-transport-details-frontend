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

package utils.cyaHelpers

import config.{FrontendAppConfig, PhaseConfig}
import models.reference.Nationality
import models.reference.transportMeans.departure.Identification
import models.{Index, Mode, Phase, UserAnswers}
import pages.transportMeans.departure.{
  AddIdentificationNumberYesNoPage,
  AddIdentificationTypeYesNoPage,
  AddVehicleCountryYesNoPage,
  IdentificationPage,
  MeansIdentificationNumberPage,
  VehicleCountryPage
}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow

class MiniTransportAnswersHelper(
  userAnswers: UserAnswers,
  mode: Mode,
  index: Index
)(implicit messages: Messages, appConfig: FrontendAppConfig, phaseConfig: PhaseConfig)
    extends AnswersHelper(userAnswers, mode) {

  def addModeIdentificationNumberYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddIdentificationTypeYesNoPage(index),
    formatAnswer = formatAsYesOrNo,
    prefix = "transportMeans.departure.addIdentificationTypeYesNo",
    id = Some("")
  )

  def identificationOfMode: Option[SummaryListRow] = getAnswerAndBuildRow[Identification](
    page = IdentificationPage(index),
    formatAnswer = formatAsText,
    prefix = "transportMeans.departure.meansIdentificationNumber",
    id = Some("")
  )

  def addIdentificationNumberYesNo: Option[SummaryListRow] = phaseConfig.phase match {
    case Phase.Transition =>
      getAnswerAndBuildRow[Boolean](
        page = AddIdentificationNumberYesNoPage(index),
        formatAnswer = formatAsYesOrNo,
        prefix = "transportMeans.departure.addIdentificationNumberYesNo",
        id = Some("")
      )
    case Phase.PostTransition => None
  }

  def identificationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = MeansIdentificationNumberPage(index),
    formatAnswer = formatAsText,
    prefix = "transportMeans.departure.identification",
    id = Some("")
  )

  def addTransportNationalityYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddVehicleCountryYesNoPage(index),
    formatAnswer = formatAsYesOrNo,
    prefix = "transportMeans.departure.addVehicleCountryYesNo",
    id = Some("")
  )

  def identificationNationality: Option[SummaryListRow] = getAnswerAndBuildRow[Nationality](
    page = VehicleCountryPage(index),
    formatAnswer = formatAsText,
    prefix = "transportMeans.departure.vehicleCountry",
    id = Some("")
  )

}
