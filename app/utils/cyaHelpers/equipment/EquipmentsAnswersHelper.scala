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
import models.journeyDomain.equipment.EquipmentDomain
import models.{Mode, UserAnswers}
import pages.equipment.AddTransportEquipmentYesNoPage
import pages.equipment.index.ContainerIdentificationNumberPage
import pages.sections.equipment.EquipmentsSection
import play.api.i18n.Messages
import utils.cyaHelpers.{AnswersHelper, RichListItems}
import viewModels.ListItem

class EquipmentsAnswersHelper(
  userAnswers: UserAnswers,
  mode: Mode
)(implicit messages: Messages, appConfig: FrontendAppConfig)
    extends AnswersHelper(userAnswers, mode) {

  def listItems: Seq[Either[ListItem, ListItem]] =
    buildListItems(EquipmentsSection) {
      equipmentIndex =>
        buildListItem[EquipmentDomain](
          nameWhenComplete = _.asString,
          nameWhenInProgress = Some(EquipmentDomain.asString(userAnswers.get(ContainerIdentificationNumberPage(equipmentIndex)), equipmentIndex)),
          removeRoute = Some(routes.RemoveTransportEquipmentController.onPageLoad(lrn, mode, equipmentIndex))
        )(EquipmentDomain.userAnswersReader(equipmentIndex).apply(Nil))
    }.checkRemoveLinks(userAnswers.get(AddTransportEquipmentYesNoPage).isEmpty)
}
