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
import models.{Index, Mode, UserAnswers}
import play.api.i18n.Messages
import play.api.mvc.Call
import utils.cyaHelpers.equipment.SealsAnswersHelper
import viewModels.{AddAnotherViewModel, ListItem}

import javax.inject.Inject

case class AddAnotherSealViewModel(
  override val listItems: Seq[ListItem],
  onSubmitCall: Call
) extends AddAnotherViewModel {
  override val prefix: String = "equipment.index.addAnotherSeal"

  override def allowMore(implicit config: FrontendAppConfig): Boolean = count < config.maxSeals
}

object AddAnotherSealViewModel {

  class AddAnotherSealViewModelProvider @Inject() (implicit appConfig: FrontendAppConfig) {

    def apply(userAnswers: UserAnswers, mode: Mode, equipmentIndex: Index)(implicit messages: Messages): AddAnotherSealViewModel = {
      val helper = new SealsAnswersHelper(userAnswers, mode, equipmentIndex)

      val listItems = helper.listItems.collect {
        case Left(value)  => value
        case Right(value) => value
      }

      new AddAnotherSealViewModel(
        listItems,
        onSubmitCall = controllers.equipment.index.routes.AddAnotherSealController.onSubmit(userAnswers.lrn, mode, equipmentIndex)
      )
    }
  }
}
