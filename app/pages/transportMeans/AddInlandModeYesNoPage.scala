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

package pages.transportMeans

import controllers.transportMeans.routes
import models.transportMeans.InlandModeYesNo
import models.transportMeans.InlandModeYesNo._
import models.{Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.TransportSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object AddInlandModeYesNoPage extends QuestionPage[InlandModeYesNo] {

  override def path: JsPath = TransportSection.path \ toString

  override def toString: String = "addInlandModeYesNo"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.AddInlandModeYesNoController.onPageLoad(userAnswers.lrn, mode))

  override def cleanup(value: Option[InlandModeYesNo], userAnswers: UserAnswers): Try[UserAnswers] = value match {
    case Some(No) => userAnswers.remove(InlandModePage)
    case _        => super.cleanup(value, userAnswers)
  }
}
