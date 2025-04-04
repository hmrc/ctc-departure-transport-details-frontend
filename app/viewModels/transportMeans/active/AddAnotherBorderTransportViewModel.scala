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

package viewModels.transportMeans.active

import config.FrontendAppConfig
import models.{Mode, UserAnswers}
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.Aliases.Content
import uk.gov.hmrc.govukfrontend.views.html.components.implicits.*
import utils.cyaHelpers.transportMeans.active.ActiveBorderTransportsAnswersHelper
import viewModels.{AddAnotherViewModel, ListItem}

import javax.inject.Inject

case class AddAnotherBorderTransportViewModel(
  override val listItems: Seq[ListItem],
  onSubmitCall: Call
) extends AddAnotherViewModel {
  override val prefix: String = "transportMeans.active.addAnotherBorderTransport"

  override def allowMore(implicit config: FrontendAppConfig): Boolean = count < config.maxActiveBorderTransports

  def hint(implicit messages: Messages): Content = messages(s"$prefix.hint").toText
}

object AddAnotherBorderTransportViewModel {

  class AddAnotherBorderTransportViewModelProvider @Inject() (implicit config: FrontendAppConfig) {

    def apply(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages): AddAnotherBorderTransportViewModel = {
      val helper = new ActiveBorderTransportsAnswersHelper(userAnswers, mode)

      val listItems = helper.listItems.collect {
        case Left(value)  => value
        case Right(value) => value
      }

      new AddAnotherBorderTransportViewModel(
        listItems,
        onSubmitCall = controllers.transportMeans.active.routes.AddAnotherBorderTransportController.onSubmit(userAnswers.lrn, mode)
      )
    }
  }
}
