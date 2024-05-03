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

package pages.additionalReference.index

import org.scalacheck.Arbitrary._
import pages.behaviours.PageBehaviours

class AddAdditionalReferenceNumberYesNoPageSpec extends PageBehaviours {

  "AddAdditionalReferenceNumberYesNoPage" - {

    beRetrievable[Boolean](AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex))

    beSettable[Boolean](AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex))

    beRemovable[Boolean](AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex))
  }

  "cleanup" - {
    "when no selected" - {
      "must remove reference number" in {
        forAll(arbitrary[String]) {
          referenceNumber =>
            val userAnswers = emptyUserAnswers
              .setValue(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex), true)
              .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), referenceNumber)

            val result = userAnswers.setValue(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex), false)

            result.get(AdditionalReferenceNumberPage(additionalReferenceIndex)) must not be defined
        }
      }
    }

    "when yes selected" - {
      "must do nothing" in {
        forAll(arbitrary[String]) {
          number =>
            val userAnswers = emptyUserAnswers
              .setValue(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex), true)
              .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), number)

            val result = userAnswers.setValue(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex), true)

            result.get(AdditionalReferenceNumberPage(additionalReferenceIndex)) must be(defined)
        }
      }
    }
  }
}
