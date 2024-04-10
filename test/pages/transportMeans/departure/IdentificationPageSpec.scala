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
import models.reference.transportMeans.departure.Identification
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class IdentificationPageSpec extends PageBehaviours {

  "IdentificationPage" - {

    beRetrievable[Identification](IdentificationPage(departureIndex))

    beSettable[Identification](IdentificationPage(departureIndex))

    beRemovable[Identification](IdentificationPage(departureIndex))

    "cleanup" - {
      "when answer changes" - {
        "must remove identification number and vehicle country" in {
          forAll(arbitrary[Identification]) {
            identification =>
              val userAnswers = emptyUserAnswers
                .setValue(IdentificationPage(departureIndex), identification)
                .setValue(MeansIdentificationNumberPage(departureIndex), arbitrary[String].sample.value)
                .setValue(VehicleCountryPage(departureIndex), arbitrary[Nationality].sample.value)

              forAll(arbitrary[Identification].retryUntil(_ != identification)) {
                differentIdentification =>
                  val result = userAnswers.setValue(IdentificationPage(departureIndex), differentIdentification)

                  result.get(MeansIdentificationNumberPage(departureIndex)) must not be defined
                  result.get(VehicleCountryPage(departureIndex)) must not be defined
              }
          }
        }
      }
    }
  }
}
