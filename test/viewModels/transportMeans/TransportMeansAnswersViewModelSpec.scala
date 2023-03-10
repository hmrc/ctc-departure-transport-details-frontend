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

package viewModels.transportMeans

import base.SpecBase
import generators.Generators
import models.reference.Nationality
import models.transportMeans.BorderModeOfTransport
import models.transportMeans.departure.{InlandMode, Identification => DepartureIdentification}
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.external.OfficesOfTransitSection
import pages.transportMeans.departure.InlandModePage
import pages.transportMeans.{departure, AnotherVehicleCrossingYesNoPage, BorderModeOfTransportPage}
import play.api.libs.json.{JsArray, Json}
import viewModels.transportMeans.TransportMeansAnswersViewModel.TransportMeansAnswersViewModelProvider

class TransportMeansAnswersViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mode = arbitrary[Mode].sample.value

  private val officesOfTransit: JsArray = JsArray(Seq(Json.obj("foo" -> "bar")))

  "TransportMeansAnswersViewModel" - {

    "must render an inland mode section" in {
      val userAnswers = emptyUserAnswers
        .setValue(InlandModePage, arbitrary[InlandMode].sample.value)

      val viewModelProvider = new TransportMeansAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers, mode)

      val section = result.sections.head
      section.sectionTitle.get mustBe "Inland mode of transport"
      section.rows.size mustBe 1
      section.addAnotherLink must not be defined
    }

    "must render a departure means section" in {
      val userAnswers = emptyUserAnswers
        .setValue(departure.IdentificationPage, arbitrary[DepartureIdentification].sample.value)
        .setValue(departure.MeansIdentificationNumberPage, Gen.alphaNumStr.sample.value)
        .setValue(departure.VehicleCountryPage, arbitrary[Nationality].sample.value)

      val viewModelProvider = new TransportMeansAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers, mode)

      val section = result.sections(1)
      section.sectionTitle.get mustBe "Departure means of transport"
      section.rows.size mustBe 3
      section.addAnotherLink must not be defined
    }

    "must render a border mode section" in {
      val userAnswers = emptyUserAnswers
        .setValue(AnotherVehicleCrossingYesNoPage, arbitrary[Boolean].sample.value)
        .setValue(BorderModeOfTransportPage, arbitrary[BorderModeOfTransport].sample.value)

      val viewModelProvider = new TransportMeansAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers, mode)

      val section = result.sections(2)
      section.sectionTitle.get mustBe "Border mode of transport"
      section.rows.size mustBe 2
      section.addAnotherLink must not be defined
    }

    "must render a border means section" - {

      val sectionTitle = "Border means of transport"

      "when customs office of transit is present" - {

        val baseAnswers = emptyUserAnswers.setValue(OfficesOfTransitSection, officesOfTransit)

        "when none were added" in {
          val userAnswers       = baseAnswers
          val viewModelProvider = new TransportMeansAnswersViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers, mode)
          val section           = result.sections(3)
          section.sectionTitle.get mustBe sectionTitle
          section.rows.size mustBe 0
          section.addAnotherLink must not be defined
        }

        "when 1 or more were added" in {
          forAll(arbitrary[Mode], Gen.choose(1, frontendAppConfig.maxActiveBorderTransports)) {
            (mode, amount) =>
              val userAnswersGen = (0 until amount).foldLeft(Gen.const(baseAnswers)) {
                (acc, i) =>
                  acc.flatMap(arbitraryTransportMeansActiveAnswers(_, Index(i)))
              }
              forAll(userAnswersGen) {
                userAnswers =>
                  val viewModelProvider = new TransportMeansAnswersViewModelProvider()
                  val result            = viewModelProvider.apply(userAnswers, mode)
                  val section           = result.sections(3)
                  section.sectionTitle.get mustBe sectionTitle
                  section.rows.size mustBe amount
                  section.addAnotherLink must be(defined)
              }
          }
        }
      }

      "when customs office of transit is not present" - {

        "when none were added" in {
          val userAnswers       = emptyUserAnswers
          val viewModelProvider = new TransportMeansAnswersViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers, mode)
          val section           = result.sections(3)
          section.sectionTitle.get mustBe sectionTitle
          section.rows.size mustBe 0
          section.addAnotherLink must not be defined
        }

        "when 1 was added" in {
          val userAnswersGen = arbitraryTransportMeansActiveAnswers(emptyUserAnswers, index)
          forAll(arbitrary[Mode], userAnswersGen) {
            (mode, userAnswers) =>
              val viewModelProvider = new TransportMeansAnswersViewModelProvider()
              val result            = viewModelProvider.apply(userAnswers, mode)
              val section           = result.sections(3)
              section.sectionTitle.get mustBe sectionTitle
              section.rows.size must be > 1
              section.addAnotherLink must not be defined
          }
        }
      }
    }
  }
}
