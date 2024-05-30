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

import play.api.i18n.Messages

import javax.inject.Inject

case class AddIdentificationTypeViewModel(addInlandModeYesNo: Boolean) {

  val prefix: String = if (addInlandModeYesNo) {
    "transportMeans.departure.addIdentificationTypeYesNo.inlandModeYes"
  } else {
    "transportMeans.departure.addIdentificationTypeYesNo.inlandModeNo"
  }

  def title(implicit messages: Messages): String   = messages(s"$prefix.title")
  def heading(implicit messages: Messages): String = messages(s"$prefix.heading")

  def paragraph(implicit messages: Messages): Option[String] = if (!addInlandModeYesNo) {
    Some(messages(s"$prefix.paragraph"))
  } else {
    None
  }
}

object AddIdentificationTypeViewModel {

  class AddIdentificationTypeViewModelProvider @Inject() () {

    def apply(addInlandModeYesNo: Boolean): AddIdentificationTypeViewModel =
      new AddIdentificationTypeViewModel(addInlandModeYesNo)
  }
}
