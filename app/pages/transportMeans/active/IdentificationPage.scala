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

package pages.transportMeans.active

import controllers.transportMeans.active.routes
import models.reference.transportMeans.active.Identification
import models.{Index, Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.transportMeans.TransportMeansActiveSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

abstract class BaseIdentificationPage(index: Index) extends QuestionPage[Identification] {

  override def path: JsPath = TransportMeansActiveSection(index).path \ toString

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.IdentificationController.onPageLoad(userAnswers.lrn, mode, index))

  def cleanup(userAnswers: UserAnswers): Try[UserAnswers]

  override def cleanup(value: Option[Identification], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(_) => userAnswers.remove(IdentificationNumberPage(index)).flatMap(cleanup)
      case _       => super.cleanup(value, userAnswers)
    }
}

case class IdentificationPage(index: Index) extends BaseIdentificationPage(index) {
  override def toString: String = "identification"

  override def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(InferredIdentificationPage(index))
}

case class InferredIdentificationPage(index: Index) extends BaseIdentificationPage(index) {
  override def toString: String = "inferredIdentification"

  override def cleanup(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(IdentificationPage(index))
}
