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

package utils.cyaHelpers.equipment

import config.FrontendAppConfig
import controllers.equipment.index.routes
import models.journeyDomain.equipment.index.itemNumber.ItemNumberDomain
import models.journeyDomain.equipment.seal.SealDomain
import models.{Index, Mode, UserAnswers}
import pages.sections.equipment.{ItemNumbersSection, SealsSection}
import pages.equipment.index._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.cyaHelpers.AnswersHelper
import viewModels.Link

class EquipmentAnswersHelper(
  userAnswers: UserAnswers,
  mode: Mode,
  equipmentIndex: Index
)(implicit messages: Messages, config: FrontendAppConfig)
    extends AnswersHelper(userAnswers, mode) {

  def containerIdentificationNumberYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddContainerIdentificationNumberYesNoPage(equipmentIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "equipment.index.addContainerIdentificationNumberYesNo",
    id = Some("change-add-container-identification-number")
  )

  def containerIdentificationNumber: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = ContainerIdentificationNumberPage(equipmentIndex),
    formatAnswer = formatAsText,
    prefix = "equipment.index.containerIdentificationNumber",
    id = Some("change-container-identification-number")
  )

  def sealsYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddSealYesNoPage(equipmentIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "equipment.index.addSealYesNo",
    id = Some("change-add-seals")
  )

  def seals: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(SealsSection(equipmentIndex))(seal)

  def seal(index: Index): Option[SummaryListRow] = getAnswerAndBuildSectionRow[SealDomain](
    formatAnswer = formatAsText,
    prefix = "equipment.index.checkYourAnswers.seal",
    id = Some(s"change-seal-${index.display}"),
    args = index.display
  )(SealDomain.userAnswersReader(equipmentIndex, index))

  def addOrRemoveSeals: Option[Link] = buildLink(SealsSection(equipmentIndex)) {
    Link(
      id = "add-or-remove-seals",
      text = messages("equipment.index.checkYourAnswers.seals.addOrRemove"),
      href = routes.AddAnotherSealController.onPageLoad(userAnswers.lrn, mode, equipmentIndex).url
    )
  }

  def itemNumbersYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddGoodsItemNumberYesNoPage(equipmentIndex),
    formatAnswer = formatAsYesOrNo,
    prefix = "equipment.index.addGoodsItemNumberYesNo",
    id = Some("change-add-item-numbers")
  )

  def itemNumbers: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(ItemNumbersSection(equipmentIndex))(itemNumber)

  def itemNumber(index: Index): Option[SummaryListRow] = getAnswerAndBuildSectionRow[ItemNumberDomain](
    formatAnswer = formatAsText,
    prefix = "equipment.index.checkYourAnswers.itemNumber",
    id = Some(s"change-goods-item-number-${index.display}"),
    args = index.display
  )(ItemNumberDomain.userAnswersReader(equipmentIndex, index))

  def addOrRemoveItemNumbers: Option[Link] = buildLink(ItemNumbersSection(equipmentIndex)) {
    Link(
      id = "add-or-remove-goods-item-numbers",
      text = messages("equipment.index.checkYourAnswers.itemNumbers.addOrRemove"),
      href = routes.AddAnotherGoodsItemNumberController.onPageLoad(userAnswers.lrn, mode, equipmentIndex).url
    )
  }
}
