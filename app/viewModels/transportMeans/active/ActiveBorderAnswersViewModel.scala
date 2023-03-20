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
import models.{Index, Mode, UserAnswers}
import play.api.i18n.Messages
import utils.cyaHelpers.transportMeans.active.ActiveBorderTransportAnswersHelper
import viewModels.Section

import javax.inject.Inject

case class ActiveBorderAnswersViewModel(sections: Seq[Section])

object ActiveBorderAnswersViewModel {

  class ActiveBorderAnswersViewModelProvider @Inject() (implicit config: FrontendAppConfig) {

    def apply(userAnswers: UserAnswers, mode: Mode, index: Index)(implicit messages: Messages): ActiveBorderAnswersViewModel = {
      val helper = new ActiveBorderTransportAnswersHelper(userAnswers, mode, index)

      val activeBorderSection = Section(
        rows = Seq(
          helper.activeBorderIdentificationType,
          helper.activeBorderIdentificationNumber,
          helper.activeBorderAddNationality,
          helper.activeBorderNationality,
          helper.customsOfficeAtBorder,
          helper.activeBorderConveyanceReferenceNumberYesNo,
          helper.conveyanceReferenceNumber
        ).flatten
      )
      new ActiveBorderAnswersViewModel(Seq(activeBorderSection))
    }
  }
}
