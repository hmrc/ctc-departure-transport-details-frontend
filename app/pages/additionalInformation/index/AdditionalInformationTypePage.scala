/*
 * Copyright 2024 HM Revenue & Customs
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

package pages.additionalInformation.index

import controllers.additionalInformation.index.routes
import models.reference.additionalInformation.AdditionalInformationCode
import models.{Index, Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.additionalInformation.AdditionalInformationSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class AdditionalInformationTypePage(additionalInformationIndex: Index) extends QuestionPage[AdditionalInformationCode] {

  override def path: JsPath = AdditionalInformationSection(additionalInformationIndex).path \ toString

  override def toString: String = "type"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.AdditionalInformationTypeController.onPageLoad(additionalInformationIndex, userAnswers.lrn, mode))
}
