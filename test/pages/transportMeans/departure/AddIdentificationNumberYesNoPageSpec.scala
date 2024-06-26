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

package pages.transportMeans.departure

import pages.behaviours.PageBehaviours

class AddIdentificationNumberYesNoPageSpec extends PageBehaviours {

  "AddIdentificationNumberYesNo Page Spec" - {

    beRetrievable[Boolean](AddIdentificationNumberYesNoPage(departureIndex))

    beSettable[Boolean](AddIdentificationNumberYesNoPage(departureIndex))

    beRemovable[Boolean](AddIdentificationNumberYesNoPage(departureIndex))

    "cleanup" - {
      "when NO selected" - {
        "must clean up IdentificationNumberPage" in {
          forAll(nonEmptyString) {
            identificationNumber =>
              val userAnswers = emptyUserAnswers
                .setValue(AddIdentificationNumberYesNoPage(departureIndex), true)
                .setValue(MeansIdentificationNumberPage(departureIndex), identificationNumber)

              val result = userAnswers.setValue(AddIdentificationNumberYesNoPage(departureIndex), false)

              result.get(MeansIdentificationNumberPage(departureIndex)) must not be defined
          }
        }
      }
    }

  }
}
