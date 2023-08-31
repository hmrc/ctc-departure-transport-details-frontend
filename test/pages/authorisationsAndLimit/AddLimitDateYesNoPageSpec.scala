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

import pages.authorisationsAndLimit.authorisations.AddLimitDateYesNoPage
import pages.authorisationsAndLimit.limit.LimitDatePage
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class AddLimitDateYesNoPageSpec extends PageBehaviours {

  "AddArrivalDateYesNoPage" - {

    beRetrievable[Boolean](AddLimitDateYesNoPage)

    beSettable[Boolean](AddLimitDateYesNoPage)

    beRemovable[Boolean](AddLimitDateYesNoPage)

    "cleanup" - {
      "when NO selected" - {
        "must clean up Limit date" in {
          val preChange  = emptyUserAnswers.setValue(LimitDatePage, LocalDate.now())
          val postChange = preChange.setValue(AddLimitDateYesNoPage, false)

          postChange.get(LimitDatePage) mustNot be(defined)
        }
      }

      "when YES selected" - {
        "must do nothing" in {
          val preChange  = emptyUserAnswers.setValue(LimitDatePage, LocalDate.now())
          val postChange = preChange.setValue(AddLimitDateYesNoPage, true)

          postChange.get(LimitDatePage) must be(defined)
        }
      }
    }
  }
}
