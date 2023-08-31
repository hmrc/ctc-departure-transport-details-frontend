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

// TODO - move page, controller and view to authorisationsAndLimit.limit
package pages.authorisationsAndLimit.authorisations

import controllers.authorisationsAndLimit.routes
import models.{Mode, UserAnswers}
import pages.QuestionPage
import pages.authorisationsAndLimit.limit.LimitDatePage
import pages.sections.authorisationsAndLimit.LimitSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object AddLimitDateYesNoPage extends QuestionPage[Boolean] {

  override def path: JsPath = LimitSection.path \ toString

  override def toString: String = "addArrivalDateYesNo"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.AddLimitDateYesNoController.onPageLoad(userAnswers.lrn, mode))

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(false) => userAnswers.remove(LimitDatePage)
      case _           => super.cleanup(value, userAnswers)
    }
}
