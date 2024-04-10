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

import models.reference.transportMeans.departure.Identification
import pages.behaviours.PageBehaviours
import org.scalacheck.Arbitrary.arbitrary

class AddIdentificationTypeYesNoPageSpec extends PageBehaviours {

  "AddContactYesNoPage" - {

    beRetrievable[Boolean](AddIdentificationTypeYesNoPage(departureIndex))

    beSettable[Boolean](AddIdentificationTypeYesNoPage(departureIndex))

    beRemovable[Boolean](AddIdentificationTypeYesNoPage(departureIndex))

    "cleanup" - {
      "when no selected" - {
        "must remove identification type" in {
          forAll(arbitrary[Identification]) {
            identification =>
              val userAnswers = emptyUserAnswers
                .setValue(AddIdentificationTypeYesNoPage(departureIndex), true)
                .setValue(IdentificationPage(departureIndex), identification)

              val result = userAnswers.setValue(AddIdentificationTypeYesNoPage(departureIndex), false)

              result.get(IdentificationPage(departureIndex)) must not be defined
          }
        }
      }
    }
  }
}
