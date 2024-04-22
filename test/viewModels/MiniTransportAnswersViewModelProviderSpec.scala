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

import base.SpecBase
import config.PhaseConfig
import generators.Generators
import models.reference.transportMeans.departure.Identification
import models.reference.{BorderMode, InlandMode, Nationality}
import models.{Index, Mode, Phase}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.external.OfficesOfTransitSection
import pages.transportMeans._
import pages.transportMeans.departure.{
  AddIdentificationNumberYesNoPage,
  AddIdentificationTypeYesNoPage,
  AddVehicleCountryYesNoPage,
  IdentificationPage,
  MeansIdentificationNumberPage,
  VehicleCountryPage
}
import play.api.libs.json.{JsArray, Json}
import viewModels.MiniTransportAnswersViewModel.MiniTransportAnswersViewModelProvider
import viewModels.transportMeans.TransportMeansAnswersViewModel.TransportMeansAnswersViewModelProvider

class MiniTransportAnswersViewModelProviderSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mode = arbitrary[Mode].sample.value

  "MiniTransportAnswersViewModelProviderSpec" - {

    "must generate a MiniTransportAnswersViewModel" in {
      val userAnswers = emptyUserAnswers
        .setValue(AddIdentificationTypeYesNoPage(index), true)
        .setValue(IdentificationPage(index), Identification("type", "desc"))
        .setValue(MeansIdentificationNumberPage(index), "id-means")
        .setValue(AddVehicleCountryYesNoPage(index), true)
        .setValue(VehicleCountryPage(index), Nationality("code", "description"))

      val viewModelProvider = new MiniTransportAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers, index)

      val section = result.sections.head
      section.sectionTitle mustBe None
      section.rows.size mustBe 5
      section.addAnotherLink must not be defined
    }

  }
}
