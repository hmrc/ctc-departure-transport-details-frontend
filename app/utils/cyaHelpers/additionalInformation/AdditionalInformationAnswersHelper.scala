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

package utils.cyaHelpers.additionalInformation

import config.{FrontendAppConfig, PhaseConfig}
import models.journeyDomain.additionalReferences.AdditionalReferenceDomain
import models.{Mode, UserAnswers}
import pages.additionalReference.index.AdditionalReferenceTypePage
import pages.sections.additionalInformation.AdditionalInformationListSection
import pages.sections.additionalReference.AdditionalReferencesSection
import play.api.i18n.Messages
import play.api.mvc.Call
import utils.cyaHelpers.AnswersHelper
import viewModels.ListItem

class AdditionalInformationAnswersHelper(userAnswers: UserAnswers, mode: Mode)(implicit
  messages: Messages,
  config: FrontendAppConfig,
  phaseConfig: PhaseConfig
) extends AnswersHelper(userAnswers, mode) {

  def listItems: Seq[Either[ListItem, ListItem]] =
    buildListItems(AdditionalInformationListSection) {
      additionalInformationIndex =>
        buildListItem[AdditionalReferenceDomain](
          nameWhenComplete = _.toString,
          // TODO: update later
          nameWhenInProgress = userAnswers.get(AdditionalReferenceTypePage(additionalInformationIndex)).map(_.toString),
          removeRoute = Some(Call("GET", "#")) // TODO change when remove page done
        )(AdditionalReferenceDomain.userAnswersReader(additionalInformationIndex).apply(Nil))
    }

}
