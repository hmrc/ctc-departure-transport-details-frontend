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

import models.OptionalBoolean
import models.reference.{Country, CountryCode}
import pages.behaviours.PageBehaviours

class AddCountryOfDestinationPageSpec extends PageBehaviours {

  "AddCountryOfDestinationPage" - {

    beRetrievable[OptionalBoolean](AddCountryOfDestinationPage)

    beSettable[OptionalBoolean](AddCountryOfDestinationPage)

    beRemovable[OptionalBoolean](AddCountryOfDestinationPage)

    "cleanup" - {
      "when no/maybe selected" - {
        "must remove TransportedToSameCountryYesNoPage and ItemsDestinationCountryPage" in {
          for (selection <- Seq(OptionalBoolean.no, OptionalBoolean.maybe)) {
            val userAnswers = emptyUserAnswers
              .setValue(TransportedToSameCountryYesNoPage, true)
              .setValue(ItemsDestinationCountryPage, Country(CountryCode("GB"), "United Kingdom"))

            val result = userAnswers.setValue(AddCountryOfDestinationPage, selection)

            result.get(TransportedToSameCountryYesNoPage) must not be defined
            result.get(ItemsDestinationCountryPage) must not be defined
          }
        }
      }

      "when yes selected" - {
        "must do nothing" in {
          val userAnswers = emptyUserAnswers
            .setValue(TransportedToSameCountryYesNoPage, true)
            .setValue(ItemsDestinationCountryPage, Country(CountryCode("GB"), "United Kingdom"))

          val result = userAnswers.setValue(AddCountryOfDestinationPage, OptionalBoolean.yes)

          result.get(TransportedToSameCountryYesNoPage) must be(defined)
          result.get(ItemsDestinationCountryPage) must be(defined)
        }
      }
    }
  }
}
