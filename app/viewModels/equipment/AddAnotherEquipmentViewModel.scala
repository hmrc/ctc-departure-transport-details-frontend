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

package viewModels.equipment

import config.FrontendAppConfig
import models.{Mode, UserAnswers}
import play.api.i18n.Messages
import play.api.mvc.Call
import utils.cyaHelpers.equipment.EquipmentsAnswersHelper
import viewModels.{AddAnotherViewModel, ListItem}

import javax.inject.Inject

case class AddAnotherEquipmentViewModel(
  override val listItems: Seq[ListItem],
  onSubmitCall: Call
) extends AddAnotherViewModel {
  override val prefix: String = "equipment.addAnotherEquipment"

  override def allowMore(implicit config: FrontendAppConfig): Boolean = count < config.maxEquipmentNumbers
}

object AddAnotherEquipmentViewModel {

  class AddAnotherEquipmentViewModelProvider @Inject() (implicit appConfig: FrontendAppConfig) {

    def apply(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages): AddAnotherEquipmentViewModel = {
      val helper = new EquipmentsAnswersHelper(userAnswers, mode)

      val listItems = helper.listItems.collect {
        case Left(value)  => value
        case Right(value) => value
      }

      new AddAnotherEquipmentViewModel(
        listItems,
        onSubmitCall = controllers.equipment.routes.AddAnotherEquipmentController.onSubmit(userAnswers.lrn, mode)
      )
    }
  }
}
