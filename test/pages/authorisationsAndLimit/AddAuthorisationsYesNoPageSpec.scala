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

package pages.authorisationsAndLimit

import pages.authorisationsAndLimit.authorisations.AddAuthorisationsYesNoPage
import pages.behaviours.PageBehaviours
import pages.sections.authorisationsAndLimit.AuthorisationsAndLimitSection
import play.api.libs.json.Json

class AddAuthorisationsYesNoPageSpec extends PageBehaviours {

  "AddAuthorisationsYesNoPage" - {

    beRetrievable[Boolean](AddAuthorisationsYesNoPage)

    beSettable[Boolean](AddAuthorisationsYesNoPage)

    beRemovable[Boolean](AddAuthorisationsYesNoPage)

    "cleanup" - {
      "when NO selected" - {
        "must clean up Authorisation section" in {
          val preChange  = emptyUserAnswers.setValue(AuthorisationsAndLimitSection, Json.obj("foo" -> "bar"))
          val postChange = preChange.setValue(AddAuthorisationsYesNoPage, false)

          postChange.get(AuthorisationsAndLimitSection) mustNot be(defined)
        }
      }

      "when YES selected" - {
        "must do nothing" in {
          val preChange  = emptyUserAnswers.setValue(AuthorisationsAndLimitSection, Json.obj("foo" -> "bar"))
          val postChange = preChange.setValue(AddAuthorisationsYesNoPage, true)

          postChange.get(AuthorisationsAndLimitSection) must be(defined)
        }
      }
    }
  }
}
