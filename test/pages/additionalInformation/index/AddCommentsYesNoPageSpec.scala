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

package pages.additionalInformation.index

import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import pages.behaviours.PageBehaviours

class AddCommentsYesNoPageSpec extends PageBehaviours {

  "AddAdditionalReferenceYesNoPage" - {

    beRetrievable[Boolean](AddCommentsYesNoPage(index))

    beSettable[Boolean](AddCommentsYesNoPage(index))

    beRemovable[Boolean](AddCommentsYesNoPage(index))

    "cleanup" - {
      "when no selected" - {
        "must remove additional information text" in {
          forAll(Gen.alphaNumStr) {
            text =>
              val userAnswers = emptyUserAnswers
                .setValue(AddCommentsYesNoPage(additionalInformationIndex), true)
                .setValue(AdditionalInformationTextPage(additionalInformationIndex), text)

              val result = userAnswers.setValue(AddCommentsYesNoPage(additionalInformationIndex), false)

              result.get(AdditionalInformationTextPage(additionalInformationIndex)) must not be defined
          }
        }
      }

      "when yes selected" - {
        "must not remove additional information text" in {
          forAll(Gen.alphaNumStr) {
            text =>
              val userAnswers = emptyUserAnswers
                .setValue(AddCommentsYesNoPage(additionalInformationIndex), true)
                .setValue(AdditionalInformationTextPage(additionalInformationIndex), text)

              val result = userAnswers.setValue(AddCommentsYesNoPage(additionalInformationIndex), true)

              result.get(AdditionalInformationTextPage(additionalInformationIndex)) must be(defined)
          }
        }
      }
    }
  }
}
