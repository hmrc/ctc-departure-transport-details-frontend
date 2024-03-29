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

package pages.transportMeans

import models.reference.BorderMode
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class AddBorderModeOfTransportYesNoPageSpec extends PageBehaviours {

  "AddBorderModeOfTransportYesNoPage" - {

    beRetrievable[Boolean](AddBorderModeOfTransportYesNoPage)

    beSettable[Boolean](AddBorderModeOfTransportYesNoPage)

    beRemovable[Boolean](AddBorderModeOfTransportYesNoPage)

    "cleanup" - {
      "when no selected" - {
        "must remove border mode of transport" in {
          forAll(arbitrary[BorderMode]) {
            borderModeOfTransport =>
              val userAnswers = emptyUserAnswers
                .setValue(AddBorderModeOfTransportYesNoPage, true)
                .setValue(BorderModeOfTransportPage, borderModeOfTransport)

              val result = userAnswers.setValue(AddBorderModeOfTransportYesNoPage, false)

              result.get(BorderModeOfTransportPage) must not be defined
          }
        }
      }

      "when yes selected" - {
        "must not remove border mode of transport" in {
          forAll(arbitrary[BorderMode]) {
            borderModeOfTransport =>
              val userAnswers = emptyUserAnswers
                .setValue(AddBorderModeOfTransportYesNoPage, true)
                .setValue(BorderModeOfTransportPage, borderModeOfTransport)

              val result = userAnswers.setValue(AddBorderModeOfTransportYesNoPage, true)

              result.get(BorderModeOfTransportPage) must be(defined)
          }
        }
      }
    }
  }
}
