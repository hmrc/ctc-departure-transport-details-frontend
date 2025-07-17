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

package viewModels.transportMeans.departure

import base.SpecBase
import generators.Generators
import models.reference.InlandMode
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.InlandModePage
import viewModels.transportMeans.departure.AddAnotherDepartureTransportMeansViewModel.AddAnotherDepartureTransportMeansViewModelProvider

class AddAnotherDepartureTransportMeansViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must get list items" - {

    "when there is one departure transport means" - {
      "when inland mode is ROAD" in {
        forAll(arbitrary[Mode]) {
          mode =>
            val answers     = emptyUserAnswers.setValue(InlandModePage, InlandMode("3", "Road"))
            val userAnswers = arbitraryTransportMeansDepartureAnswers(answers, departureIndex).sample.value

            val result = new AddAnotherDepartureTransportMeansViewModelProvider()(frontendAppConfig)(userAnswers, mode)
            result.listItems.length mustEqual 1
            result.title mustEqual "You have added 1 departure means of transport"
            result.heading mustEqual "You have added 1 departure means of transport"
            result.legend mustEqual "Do you want to add another departure means of transport?"
            result.maxLimitLabel mustEqual "You can only add up to 3 departure means of transport when using road as your mode. To add another, you need to remove one first."
        }
      }

      "when inland mode is not ROAD" in {
        forAll(arbitrary[Mode]) {
          mode =>
            val inlandMode  = arbitrary[InlandMode](arbitraryNonRoadInlandMode).sample.value
            val answers     = emptyUserAnswers.setValue(InlandModePage, inlandMode)
            val userAnswers = arbitraryTransportMeansDepartureAnswers(answers, departureIndex).sample.value

            val result = new AddAnotherDepartureTransportMeansViewModelProvider()(frontendAppConfig)(userAnswers, mode)
            result.listItems.length mustEqual 1
            result.title mustEqual "You have added 1 departure means of transport"
            result.heading mustEqual "You have added 1 departure means of transport"
            result.legend mustEqual "Do you want to add another departure means of transport?"
            result.maxLimitLabel mustEqual "You cannot add any more departure means of transport. To add another, you need to remove one first."
        }
      }

    }

    "when there are multiple departure transport means" in {
      val formatter  = java.text.NumberFormat.getIntegerInstance
      val inlandMode = arbitrary[InlandMode](arbitraryNonRoadInlandMode).sample.value
      val answers    = emptyUserAnswers.setValue(InlandModePage, inlandMode)

      forAll(arbitrary[Mode], Gen.choose(2, frontendAppConfig.maxDepartureTransportMeans)) {
        (mode, departureTransportMeans) =>
          val userAnswers = (0 until departureTransportMeans).foldLeft(answers) {
            (acc, i) =>
              arbitraryTransportMeansDepartureAnswers(acc, Index(i)).sample.value
          }

          val result = new AddAnotherDepartureTransportMeansViewModelProvider()(frontendAppConfig)(userAnswers, mode)
          result.listItems.length mustEqual departureTransportMeans
          result.title mustEqual s"You have added ${formatter.format(departureTransportMeans)} departure means of transport"
          result.heading mustEqual s"You have added ${formatter.format(departureTransportMeans)} departure means of transport"
          result.legend mustEqual "Do you want to add another departure means of transport?"
          result.maxLimitLabel mustEqual "You cannot add any more departure means of transport. To add another, you need to remove one first."
      }
    }
  }
}
