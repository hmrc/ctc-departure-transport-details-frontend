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

package viewModels

import config.{FrontendAppConfig, PhaseConfig}
import models.{CheckMode, Index, UserAnswers}
import play.api.i18n.Messages
import utils.cyaHelpers.transportMeans.departure.DepartureTransportMeansAnswersHelper

import javax.inject.Inject

case class DepartureTransportAnswersViewModel(sections: Seq[Section])

object DepartureTransportAnswersViewModel {

  class DepartureTransportAnswersViewModelProvider @Inject() (implicit
    val config: FrontendAppConfig
  ) {

    // scalastyle:off method.length
    def apply(userAnswers: UserAnswers, index: Index)(implicit messages: Messages, phaseConfig: PhaseConfig): DepartureTransportAnswersViewModel = {
      val mode = CheckMode

      val helper = new DepartureTransportMeansAnswersHelper(userAnswers, mode, index)

      val preRequisitesSection = Section(
        rows = Seq(
          helper.departureAddTypeYesNo(Some("checkYourAnswers")),
          helper.departureIdentificationType,
          helper.departureAddIdentificationNumber,
          helper.departureIdentificationNumber,
          helper.departureAddNationality,
          helper.departureNationality
        ).flatten
      )

      val sections = preRequisitesSection.toSeq
      new DepartureTransportAnswersViewModel(sections)
    }
    // scalastyle:on method.length
  }
}
