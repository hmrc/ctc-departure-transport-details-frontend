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

package pages.additionalInformation

import org.scalacheck.Arbitrary._
import pages.behaviours.PageBehaviours
import pages.sections.additionalInformation.AdditionalInformationListSection
import play.api.libs.json.{JsArray, Json}

class AddAdditionalInformationYesNoPageSpec extends PageBehaviours {

  "AddAdditionalReferenceYesNoPage" - {

    beRetrievable[Boolean](AddAdditionalInformationYesNoPage)

    beSettable[Boolean](AddAdditionalInformationYesNoPage)

    beRemovable[Boolean](AddAdditionalInformationYesNoPage)
  }

  "cleanup" - {
    "when no is selected" - {
      "must remove additional information section" in {
        val userAnswers = emptyUserAnswers
          .setValue(AdditionalInformationListSection, JsArray(Seq(Json.obj("foo" -> "bar"))))

        val result = userAnswers.setValue(AddAdditionalInformationYesNoPage, false)

        result.get(AdditionalInformationListSection) mustNot be(defined)
      }
    }

    "when yes is selected" - {
      "must not remove additional information section" in {
        val userAnswers = emptyUserAnswers
          .setValue(AdditionalInformationListSection, JsArray(Seq(Json.obj("foo" -> "bar"))))

        val result = userAnswers.setValue(AddAdditionalInformationYesNoPage, true)

        result.get(AdditionalInformationListSection) must be(defined)
      }
    }
  }
}
