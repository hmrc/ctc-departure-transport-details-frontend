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

import models.reference.Nationality
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class AddVehicleCountryYesNoPageSpec extends PageBehaviours {

  "AddVehicleCountryYesNo Page Spec" - {

    beRetrievable[Boolean](AddVehicleCountryYesNoPage(departureIndex))

    beSettable[Boolean](AddVehicleCountryYesNoPage(departureIndex))

    beRemovable[Boolean](AddVehicleCountryYesNoPage(departureIndex))

    "cleanup" - {
      "when NO selected" - {
        "must clean up Vehicle Country Page" in {
          forAll(arbitrary[Nationality]) {
            nationality =>
              val userAnswers = emptyUserAnswers
                .setValue(AddVehicleCountryYesNoPage(departureIndex), true)
                .setValue(VehicleCountryPage(departureIndex), nationality)

              val result = userAnswers.setValue(AddVehicleCountryYesNoPage(departureIndex), false)

              result.get(VehicleCountryPage(departureIndex)) must not be defined
          }
        }
      }
    }

  }
}
