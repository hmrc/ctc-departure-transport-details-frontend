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

import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.sections.external.ItemsSection
import play.api.libs.json.{JsArray, Json}

class TransportedToSameCountryYesNoPageSpec extends PageBehaviours {

  private val array = JsArray(Seq(Json.obj("foo" -> "bar")))

  "TransportedToSameCountryYesNoPage" - {

    beRetrievable[Boolean](TransportedToSameCountryYesNoPage)

    beSettable[Boolean](TransportedToSameCountryYesNoPage)

    beRemovable[Boolean](TransportedToSameCountryYesNoPage)

    "cleanup" - {
      "when no selected" - {
        "must remove country of destination answer and Items Section" in {
          val userAnswers = emptyUserAnswers
            .setValue(ItemsDestinationCountryPage, arbitrary[Country].sample.value)
            .setValue(ItemsSection, array)

          val result = userAnswers.setValue(TransportedToSameCountryYesNoPage, false)

          result.get(ItemsDestinationCountryPage) must not be defined
          result.get(ItemsSection) must not be defined
        }
      }
    }
  }
}
