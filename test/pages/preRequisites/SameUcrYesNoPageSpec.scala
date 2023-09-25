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

package pages.preRequisites

import pages.behaviours.PageBehaviours
import pages.sections.external.ItemsSection
import play.api.libs.json.{JsArray, Json}

class SameUcrYesNoPageSpec extends PageBehaviours {
  private val array = JsArray(Seq(Json.obj("foo" -> "bar")))

  "SameUcrYesNoPage" - {

    beRetrievable[Boolean](SameUcrYesNoPage)

    beSettable[Boolean](SameUcrYesNoPage)

    beRemovable[Boolean](SameUcrYesNoPage)

    "cleanup" - {
      "when no selected" - {
        "must remove unique consignment reference number and Items sections" in {
          val userAnswers = emptyUserAnswers
            .setValue(UniqueConsignmentReferencePage, nonEmptyString.sample.value)
            .setValue(ItemsSection, array)

          val result = userAnswers.setValue(SameUcrYesNoPage, false)

          result.get(UniqueConsignmentReferencePage) must not be defined
          result.get(ItemsSection) must not be defined
        }
      }
    }
  }
}
