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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.AddInlandModeYesNoPage
import viewModels.transportMeans.departure.AddDepartureTransportMeansYesNoViewModel.AddDepartureTransportMeansYesNoViewModelProvider

class AddDepartureTransportMeansYesNoViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "must create view model" - {
    "when addInlandModeYesNo is true" in {
      val userAnswers = emptyUserAnswers
        .setValue(AddInlandModeYesNoPage, true)

      val viewModelProvider = new AddDepartureTransportMeansYesNoViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers)

      result.prefix mustEqual "transportMeans.addDepartureTransportMeansYesNo.inlandModeYes"
      result.title mustEqual "Do you want to add identification for this vehicle?"
      result.heading mustEqual "Do you want to add identification for this vehicle?"
      result.paragraph must not be defined
    }

    "when addInlandModeYesNo is false" in {
      val userAnswers = emptyUserAnswers
        .setValue(AddInlandModeYesNoPage, false)

      val viewModelProvider = new AddDepartureTransportMeansYesNoViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers)

      result.prefix mustEqual "transportMeans.addDepartureTransportMeansYesNo.inlandModeNo"
      result.title mustEqual "Do you want to add identification for the departure means of transport?"
      result.heading mustEqual "Do you want to add identification for the departure means of transport?"
      result.paragraph.value mustEqual "This is the means of transport used from the UK office of departure to a UK port or airport."
    }

  }
}
