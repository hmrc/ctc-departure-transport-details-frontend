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

package pages.preRequisites

import controllers.preRequisites.routes
import models.{Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.PreRequisitesSection
import pages.sections.external.ItemsSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.{Success, Try}

case object SameUcrYesNoPage extends QuestionPage[Boolean] {

  override def path: JsPath = PreRequisitesSection.path \ toString

  override def toString: String = "sameUcrYesNo"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.SameUcrYesNoController.onPageLoad(userAnswers.lrn, mode))

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    lazy val externalCleanup: UserAnswers => UserAnswers = _.remove(ItemsSection)

    value match {
      case Some(false) =>
        userAnswers
          .remove(UniqueConsignmentReferencePage)
          .map(externalCleanup)
      case Some(_) => Success(externalCleanup(userAnswers))
      case _       => super.cleanup(value, userAnswers)
    }
  }
}
