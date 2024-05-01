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

package pages.additionalReference

import org.scalacheck.Arbitrary._
import pages.behaviours.PageBehaviours
import pages.sections.additionalReference.AdditionalReferencesSection
import play.api.libs.json.{JsArray, Json}

class AddAdditionalReferenceYesNoPageSpec extends PageBehaviours {

  "AddAdditionalReferenceYesNoPage" - {

    beRetrievable[Boolean](AddAdditionalReferenceYesNoPage)

    beSettable[Boolean](AddAdditionalReferenceYesNoPage)

    beRemovable[Boolean](AddAdditionalReferenceYesNoPage)
  }

  "cleanup" - {
    "when no is selected" - {
      "must remove additional references section" in {
        val userAnswers = emptyUserAnswers
          .setValue(AdditionalReferencesSection, JsArray(Seq(Json.obj("foo" -> "bar"))))

        val result = userAnswers.setValue(AddAdditionalReferenceYesNoPage, false)

        result.get(AdditionalReferencesSection) mustNot be(defined)
      }
    }

    "when yes is selected" - {
      "must not remove additional references section" in {
        val userAnswers = emptyUserAnswers
          .setValue(AdditionalReferencesSection, JsArray(Seq(Json.obj("foo" -> "bar"))))

        val result = userAnswers.setValue(AddAdditionalReferenceYesNoPage, true)

        result.get(AdditionalReferencesSection) must be(defined)
      }
    }
  }
}
