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
import controllers.equipment.index.routes
import models.{Index, Mode, UserAnswers}
import pages.equipment.AddTransportEquipmentYesNoPage
import pages.equipment.index.ContainerIdentificationNumberPage
import pages.sections.equipment.EquipmentsSection
import play.api.i18n.Messages
import play.api.libs.json.JsArray
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherEquipmentViewModel(
  override val listItems: Seq[ListItem],
  onSubmitCall: Call
) extends AddAnotherViewModel {
  override val prefix: String = "equipment.addAnotherEquipment"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxEquipmentNumbers
}

object AddAnotherEquipmentViewModel {

  class AddAnotherEquipmentViewModelProvider {

    def apply(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages): AddAnotherEquipmentViewModel = {

      val listItems = userAnswers
        .get(EquipmentsSection)
        .getOrElse(JsArray())
        .value
        .zipWithIndex
        .flatMap {
          case (_, i) =>
            val equipmentIndex = Index(i)

            def equipmentPrefix(increment: Int)(implicit messages: Messages) = messages("equipment.prefix", increment)
            val containerPrefix                                              = messages("equipment.containerPrefix")
            val noContainer                                                  = messages("equipment.value.withoutContainer")

            val name = userAnswers.get(ContainerIdentificationNumberPage(equipmentIndex)) match {
              case Some(identificationNumber) => Some(s"${equipmentPrefix(equipmentIndex.display)} - $containerPrefix $identificationNumber")
              case _                          => Some(s"${equipmentPrefix(equipmentIndex.display)} - ${noContainer.toLowerCase}")
            }

            val changeRoute = routes.EquipmentAnswersController.onPageLoad(userAnswers.lrn, mode, equipmentIndex).url

            val removeRoute: Option[String] = if (equipmentIndex.isFirst && userAnswers.get(AddTransportEquipmentYesNoPage).isEmpty) {
              None
            } else {
              Some(routes.RemoveTransportEquipmentController.onPageLoad(userAnswers.lrn, mode, equipmentIndex).url)
            }

            name.map(ListItem(_, changeRoute, removeRoute))
        }
        .toSeq

      new AddAnotherEquipmentViewModel(
        listItems,
        onSubmitCall = controllers.equipment.routes.AddAnotherEquipmentController.onSubmit(userAnswers.lrn, mode)
      )
    }
  }
}
