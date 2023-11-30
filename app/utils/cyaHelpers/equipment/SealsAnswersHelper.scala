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

import config.{FrontendAppConfig, PhaseConfig}
import controllers.equipment.index.seals.routes
import models.journeyDomain.equipment.seal.SealDomain
import models.{Index, Mode, UserAnswers}
import pages.equipment.index.AddSealYesNoPage
import pages.equipment.index.seals.IdentificationNumberPage
import pages.sections.equipment.SealsSection
import play.api.i18n.Messages
import utils.cyaHelpers.{AnswersHelper, RichListItems}
import viewModels.ListItem

class SealsAnswersHelper(
  userAnswers: UserAnswers,
  mode: Mode,
  equipmentIndex: Index
)(implicit messages: Messages, appConfig: FrontendAppConfig, phaseConfig: PhaseConfig)
    extends AnswersHelper(userAnswers, mode) {

  def listItems: Seq[Either[ListItem, ListItem]] =
    buildListItems(SealsSection(equipmentIndex)) {
      sealIndex =>
        buildListItem[SealDomain](
          nameWhenComplete = _.identificationNumber,
          nameWhenInProgress = userAnswers.get(IdentificationNumberPage(equipmentIndex, sealIndex)),
          removeRoute = Some(routes.RemoveSealYesNoController.onPageLoad(lrn, mode, equipmentIndex, sealIndex))
        )(SealDomain.userAnswersReader(equipmentIndex, sealIndex))
    }.checkRemoveLinks(userAnswers.get(AddSealYesNoPage(equipmentIndex)).isEmpty)

}
