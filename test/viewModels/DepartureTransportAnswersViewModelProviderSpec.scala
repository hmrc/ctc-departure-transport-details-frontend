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

package viewModels

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.reference.Nationality
import models.reference.transportMeans.departure.Identification
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.AddInlandModeYesNoPage
import pages.transportMeans.departure.*
import viewModels.DepartureTransportAnswersViewModel.DepartureTransportAnswersViewModelProvider

class DepartureTransportAnswersViewModelProviderSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "MiniTransportAnswersViewModelProviderSpec" - {

    "must generate a MiniTransportAnswersViewModel" in {
      val userAnswers = emptyUserAnswers
        .setValue(AddInlandModeYesNoPage, true)
        .setValue(AddIdentificationTypeYesNoPage(index), true)
        .setValue(IdentificationPage(index), Identification("type", "desc"))
        .setValue(AddIdentificationNumberYesNoPage(index), true)
        .setValue(MeansIdentificationNumberPage(index), "id-means")
        .setValue(AddVehicleCountryYesNoPage(index), true)
        .setValue(VehicleCountryPage(index), Nationality("code", "description"))

      val viewModelProvider = new DepartureTransportAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers, index)

      val section = result.sections.head
      section.sectionTitle must not be defined
      section.rows.size mustEqual 6
      section.addAnotherLink must not be defined
    }

  }
}
