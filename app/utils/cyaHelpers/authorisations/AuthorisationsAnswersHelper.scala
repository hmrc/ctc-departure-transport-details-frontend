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

package utils.cyaHelpers.authorisations

import config.{FrontendAppConfig, PhaseConfig}
import controllers.authorisationsAndLimit.authorisations.index.routes
import models.journeyDomain.authorisationsAndLimit.authorisations.AuthorisationDomain
import models.{Index, Mode, UserAnswers}
import pages.sections.authorisationsAndLimit.AuthorisationsSection
import pages.authorisationsAndLimit.authorisations.AddAuthorisationsYesNoPage
import pages.authorisationsAndLimit.authorisations.index.{AuthorisationTypePage, InferredAuthorisationTypePage}
import play.api.i18n.Messages
import play.api.mvc.Call
import utils.cyaHelpers.AnswersHelper
import viewModels.ListItem

class AuthorisationsAnswersHelper(
  userAnswers: UserAnswers,
  mode: Mode
)(implicit messages: Messages, appConfig: FrontendAppConfig, phaseConfig: PhaseConfig)
    extends AnswersHelper(userAnswers, mode) {

  def listItems: Seq[Either[ListItem, ListItem]] =
    buildListItems(AuthorisationsSection) {
      index =>
        def removeRoute(authIndex: Index): Option[Call] = authIndex match {
          case Index(0) if userAnswers.get(AddAuthorisationsYesNoPage).isEmpty                 => None
          case Index(1) if userAnswers.get(InferredAuthorisationTypePage(authIndex)).isDefined => None
          case _                                                                               => Some(routes.RemoveAuthorisationYesNoController.onPageLoad(lrn, mode, authIndex))
        }

        buildListItem[AuthorisationDomain](
          nameWhenComplete = _.asString,
          nameWhenInProgress = userAnswers.get(AuthorisationTypePage(index)).map(_.forDisplay),
          removeRoute = removeRoute(index)
        )(AuthorisationDomain.userAnswersReader(index))
    }
}
