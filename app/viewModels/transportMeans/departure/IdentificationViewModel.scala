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

import models.reference.transportMeans.departure.Identification
import models.{Index, UserAnswers}
import pages.transportMeans.departure.IdentificationPage
import play.api.i18n.Messages

import javax.inject.Inject

case class IdentificationViewModel(identification: Option[Identification]) {

  val prefix: String = "transportMeans.departure.identification"

  def title(implicit messages: Messages): String = messages(s"$prefix.title")

  def heading(implicit messages: Messages): String = messages(s"$prefix.heading")

  def paragraph1(implicit messages: Messages): String = messages(s"$prefix.paragraph1")

  def paragraph2(implicit messages: Messages): String = messages(s"$prefix.paragraph2")
}

object IdentificationViewModel {

  class IdentificationViewModelProvider @Inject() () {

    def apply(userAnswers: UserAnswers, departureIndex: Index): IdentificationViewModel =
      new IdentificationViewModel(userAnswers.get(IdentificationPage(departureIndex)))
  }
}
