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

package viewModels.transportMeans.departure

import config.{FrontendAppConfig, PhaseConfig}
import models.{Mode, UserAnswers}
import pages.transportMeans.InlandModePage
import play.api.i18n.Messages
import play.api.mvc.Call
import utils.cyaHelpers.transportMeans.departure.DeparturesTransportMeansAnswersHelper
import viewModels.{AddAnotherViewModel, ListItem}

import javax.inject.Inject

case class AddAnotherDepartureTransportMeansViewModel(
  override val listItems: Seq[ListItem],
  isRoadInlandMode: Boolean,
  onSubmitCall: Call
) extends AddAnotherViewModel {
  override val prefix: String = "transportMeans.departure.addAnotherDepartureTransportMeans"

  override def allowMore(implicit config: FrontendAppConfig): Boolean =
    if (isRoadInlandMode) count < config.maxRoadInlandModeDepartureTransportMeans else count < config.maxDepartureTransportMeans

  override def maxLimitLabel(implicit messages: Messages): String =
    if (isRoadInlandMode) messages(s"$prefix.roadInlandMode.maxLimit.label") else messages(s"$prefix.maxLimit.label")
}

object AddAnotherDepartureTransportMeansViewModel {

  class AddAnotherDepartureTransportMeansViewModelProvider @Inject() (implicit config: FrontendAppConfig) {

    def apply(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages, phaseConfig: PhaseConfig): AddAnotherDepartureTransportMeansViewModel = {
      val isRoadInlandMode: Boolean = userAnswers.get(InlandModePage).exists(_.code == "3")

      val helper = new DeparturesTransportMeansAnswersHelper(userAnswers, mode)

      val listItems = helper.listItems.collect {
        case Left(value)  => value
        case Right(value) => value
      }

      new AddAnotherDepartureTransportMeansViewModel(
        listItems,
        isRoadInlandMode,
        onSubmitCall = controllers.transportMeans.departure.routes.AddAnotherDepartureTransportMeansController.onSubmit(userAnswers.lrn, mode)
      )
    }
  }
}
