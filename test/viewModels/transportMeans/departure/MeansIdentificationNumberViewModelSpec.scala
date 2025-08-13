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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.reference.transportMeans.departure.Identification
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.departure.IdentificationPage
import viewModels.transportMeans.departure.MeansIdentificationNumberViewModel.MeansIdentificationNumberViewModelProvider

class MeansIdentificationNumberViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "must create view model" - {
    "when identification type is defined" in {
      val viewModelProvider = new MeansIdentificationNumberViewModelProvider()
      val result            = viewModelProvider.apply(emptyUserAnswers, departureIndex)

      result.prefix mustEqual "transportMeans.departure.meansIdentificationNumber.withNoIDType"
      result.title mustEqual "What is the identification for this vehicle?"
      result.heading mustEqual "What is the identification for this vehicle?"
    }

    "when identification type is not defined" in {
      forAll(arbitrary[Identification]) {
        identification =>
          val userAnswers       = emptyUserAnswers.setValue(IdentificationPage(departureIndex), identification)
          val viewModelProvider = new MeansIdentificationNumberViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers, departureIndex)

          result.prefix mustEqual "transportMeans.departure.meansIdentificationNumber.withIDType"
          result.title mustEqual "What is the identification for this vehicle?"
          result.heading mustEqual "What is the identification for this vehicle?"
      }
    }
  }
}
