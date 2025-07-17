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
import models.Mode
import models.reference.Nationality
import models.reference.transportMeans.departure.Identification
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.AddInlandModeYesNoPage
import pages.transportMeans.departure._
import viewModels.transportMeans.departure.DepartureTransportMeansAnswersViewModel.DepartureTransportMeansAnswersViewModelProvider

class DepartureTransportMeansAnswersViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val mode: Mode = arbitrary[Mode].sample.value

  "apply" - {

    val mode = arbitrary[Mode].sample.value

    "must return row for each answer" in {

      val userAnswers = emptyUserAnswers
        .setValue(AddInlandModeYesNoPage, true)
        .setValue(IdentificationPage(departureIndex), arbitrary[Identification].sample.value)
        .setValue(MeansIdentificationNumberPage(departureIndex), nonEmptyString.sample.value)
        .setValue(VehicleCountryPage(departureIndex), arbitrary[Nationality].sample.value)
        .setValue(AddIdentificationNumberYesNoPage(departureIndex), true)
        .setValue(AddIdentificationTypeYesNoPage(departureIndex), true)
        .setValue(AddVehicleCountryYesNoPage(departureIndex), true)

      val viewModelProvider              = injector.instanceOf[DepartureTransportMeansAnswersViewModelProvider]
      val sections                       = viewModelProvider.apply(userAnswers, mode, departureIndex).sections
      val departureTransportMeansSection = sections.head

      departureTransportMeansSection.sectionTitle mustNot be(defined)
      departureTransportMeansSection.rows.size mustEqual 6
      departureTransportMeansSection.addAnotherLink must not be defined
    }
  }
}
