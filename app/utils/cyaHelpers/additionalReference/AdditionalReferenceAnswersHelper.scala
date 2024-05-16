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

package utils.cyaHelpers.additionalReference

import config.{FrontendAppConfig, PhaseConfig}
import models.journeyDomain.additionalReferences.AdditionalReferenceDomain
import models.{Mode, UserAnswers}
import pages.additionalReference.index.AdditionalReferenceTypePage
import pages.sections.additionalReference.AdditionalReferencesSection
import play.api.i18n.Messages
import utils.cyaHelpers.AnswersHelper
import viewModels.ListItem

class AdditionalReferenceAnswersHelper(userAnswers: UserAnswers, mode: Mode)(implicit
  messages: Messages,
  config: FrontendAppConfig,
  phaseConfig: PhaseConfig
) extends AnswersHelper(userAnswers, mode) {

  def listItems: Seq[Either[ListItem, ListItem]] =
    buildListItems(AdditionalReferencesSection) {
      additionalReferenceIndex =>
        buildListItem[AdditionalReferenceDomain](
          nameWhenComplete = _.toString,
          nameWhenInProgress = userAnswers.get(AdditionalReferenceTypePage(additionalReferenceIndex)).map(_.toString),
          removeRoute = Some(
            controllers.additionalReference.index.routes.RemoveAdditionalReferenceYesNoController.onPageLoad(userAnswers.lrn, mode, additionalReferenceIndex)
          )
        )(AdditionalReferenceDomain.userAnswersReader(additionalReferenceIndex).apply(Nil))
    }

}
