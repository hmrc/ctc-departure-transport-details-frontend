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

package models.removable

import models.reference.additionalInformation.AdditionalInformationCode
import models.{Index, UserAnswers}
import pages.additionalInformation.index.AdditionalInformationTypePage
import play.api.libs.json.Reads

case class AdditionalInformation(`type`: AdditionalInformationCode) {
  def forRemoveDisplay: String = `type`.toString
}

object AdditionalInformation {

  def apply(userAnswers: UserAnswers, additionalInformationIndex: Index): Option[AdditionalInformation] = {
    implicit val reads: Reads[AdditionalInformation] =
      AdditionalInformationTypePage(additionalInformationIndex).path
        .read[AdditionalInformationCode]
        .map {
          additionalInformationCode => AdditionalInformation(additionalInformationCode)
        }
    userAnswers.data.asOpt[AdditionalInformation]
  }

}
