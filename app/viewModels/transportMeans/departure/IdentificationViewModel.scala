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

package viewModels.transportMeans.departure

import config.Constants.ModeOfTransport.Road
import models.reference.InlandMode
import models.reference.transportMeans.departure.Identification
import models.{Index, UserAnswers}
import pages.transportMeans.InlandModePage
import pages.transportMeans.departure.IdentificationPage
import play.api.i18n.Messages

import javax.inject.Inject

case class IdentificationViewModel(identification: Option[Identification], departureIndex: Index, inlandMode: Option[InlandMode]) {

  val prefix: String = "transportMeans.departure.identification"

  def title(implicit messages: Messages): String = messages(s"$prefix.title")

  def heading(implicit messages: Messages): String = messages(s"$prefix.heading")

  def paragraph1(implicit messages: Messages): String = messages(s"$prefix.paragraph1")

  def paragraph2(implicit messages: Messages): String = messages(s"$prefix.paragraph2")

  def para(implicit messages: Messages): Option[String] = (inlandMode, departureIndex) match {
    case (Some(InlandMode(Road, _)), Index(0)) => Some(paragraph1)
    case (Some(InlandMode(Road, _)), _)        => Some(paragraph2)
    case _                                     => None
  }
}

object IdentificationViewModel {

  class IdentificationViewModelProvider @Inject() () {

    def apply(userAnswers: UserAnswers, departureIndex: Index): IdentificationViewModel =
      new IdentificationViewModel(userAnswers.get(IdentificationPage(departureIndex)), departureIndex, userAnswers.get(InlandModePage))
  }
}
