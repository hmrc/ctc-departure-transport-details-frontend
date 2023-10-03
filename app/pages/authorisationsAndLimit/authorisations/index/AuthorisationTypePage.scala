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

package pages.authorisationsAndLimit.authorisations.index

import controllers.authorisationsAndLimit.authorisations.index.routes
import models.reference.authorisations.AuthorisationType
import models.{Index, Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.authorisationsAndLimit.{AuthorisationSection, LimitSection}
import pages.sections.equipment.EquipmentsSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

abstract class BaseAuthorisationTypePage(authorisationIndex: Index) extends QuestionPage[AuthorisationType] {

  override def path: JsPath = AuthorisationSection(authorisationIndex).path \ toString

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.AuthorisationTypeController.onPageLoad(userAnswers.lrn, mode, authorisationIndex))

  def cleanup(userAnswers: UserAnswers): Try[UserAnswers]

  // TODO - if user adds or removes a SSE type authorisation this affects the transport equipment seals and goods item numbers nav.
  override def cleanup(value: Option[AuthorisationType], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(_) =>
        userAnswers
          .remove(AuthorisationReferenceNumberPage(authorisationIndex))
          .flatMap(_.remove(LimitSection))
          .flatMap(_.remove(EquipmentsSection))
          .flatMap(cleanup)
      case _ => super.cleanup(value, userAnswers)
    }
}

case class AuthorisationTypePage(authorisationIndex: Index) extends BaseAuthorisationTypePage(authorisationIndex) {
  override def toString: String = "authorisationType"

  override def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(InferredAuthorisationTypePage(authorisationIndex))
}

case class InferredAuthorisationTypePage(authorisationIndex: Index) extends BaseAuthorisationTypePage(authorisationIndex) {
  override def toString: String = "inferredAuthorisationType"

  override def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(AuthorisationTypePage(authorisationIndex))
}
