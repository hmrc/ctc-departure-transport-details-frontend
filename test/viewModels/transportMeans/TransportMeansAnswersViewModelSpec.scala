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
import models.reference.{BorderMode, InlandMode}
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.external.OfficesOfTransitSection
import pages.transportMeans._
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
      section.sectionTitle.get mustEqual "Inland mode of transport"
      section.rows.size mustEqual 1
      section.addAnotherLink must not be defined
    }

    "must render a departure means section" - {

      val sectionTitle = "Departure means of transport"

      "when none were added" in {
        val userAnswers       = emptyUserAnswers.setValue(AddDepartureTransportMeansYesNoPage, false)
        val viewModelProvider = new TransportMeansAnswersViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers, mode)(messages)
        val section           = result.sections(1)
        section.sectionTitle.get mustEqual sectionTitle
        section.rows.size mustEqual 1
        section.addAnotherLink must not be defined
      }

      "when 1 or more were added" in {
        val initialAnswers = emptyUserAnswers.setValue(AddDepartureTransportMeansYesNoPage, true)
        forAll(arbitrary[Mode], Gen.choose(1, frontendAppConfig.maxDepartureTransportMeans)) {
          (mode, amount) =>
            val userAnswersGen = (0 until amount).foldLeft(Gen.const(initialAnswers)) {
              (acc, i) =>
                acc.flatMap(arbitraryTransportMeansDepartureAnswers(_, Index(i)))
            }
            forAll(userAnswersGen) {
              userAnswers =>
                val viewModelProvider = new TransportMeansAnswersViewModelProvider()
                val result            = viewModelProvider.apply(userAnswers, mode)(messages)
                val section           = result.sections(1)
                section.sectionTitle.get mustEqual sectionTitle
                section.rows.size mustEqual amount + 1
                section.addAnotherLink must be(defined)
            }
        }
      }
    }

    "must render a border mode section" in {
      val userAnswers = emptyUserAnswers
        .setValue(AddBorderModeOfTransportYesNoPage, true)
        .setValue(BorderModeOfTransportPage, arbitrary[BorderMode].sample.value)

      val viewModelProvider = new TransportMeansAnswersViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers, mode)

      val section = result.sections(2)
      section.sectionTitle.get mustEqual "Border mode of transport"
      section.rows.size mustEqual 2
      section.addAnotherLink must not be defined
    }

    "must render a border means section" - {

      val sectionTitle = "Border means of transport"

      "when customs office of transit is present" - {

        val baseAnswers = emptyUserAnswers.setValue(OfficesOfTransitSection, officesOfTransit)

        "when none were added" in {
          val userAnswers       = baseAnswers.setValue(AddActiveBorderTransportMeansYesNoPage, false)
          val viewModelProvider = new TransportMeansAnswersViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers, mode)(messages)
          val section           = result.sections(3)
          section.sectionTitle.get mustEqual sectionTitle
          section.rows.size mustEqual 1
          section.addAnotherLink must not be defined
        }

        "when 1 or more were added" in {
          val initialAnswers = baseAnswers.setValue(AddActiveBorderTransportMeansYesNoPage, true)
          forAll(arbitrary[Mode], Gen.choose(1, frontendAppConfig.maxActiveBorderTransports)) {
            (mode, amount) =>
              val userAnswersGen = (0 until amount).foldLeft(Gen.const(initialAnswers)) {
                (acc, i) =>
                  acc.flatMap(arbitraryTransportMeansActiveAnswers(_, Index(i)))
              }
              forAll(userAnswersGen) {
                userAnswers =>
                  val viewModelProvider = new TransportMeansAnswersViewModelProvider()
                  val result            = viewModelProvider.apply(userAnswers, mode)(messages)
                  val section           = result.sections(3)
                  section.sectionTitle.get mustEqual sectionTitle
                  section.rows.size mustEqual amount + 1
                  section.addAnotherLink must be(defined)
              }
          }
        }
      }

      "when customs office of transit is not present" - {

        "when none were added" in {
          val userAnswers       = emptyUserAnswers.setValue(AddActiveBorderTransportMeansYesNoPage, false)
          val viewModelProvider = new TransportMeansAnswersViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers, mode)(messages)
          val section           = result.sections(3)
          section.sectionTitle.get mustEqual sectionTitle
          section.rows.size mustEqual 1
          section.addAnotherLink must not be defined
        }

        "when 1 was added" in {
          val initialAnswers = emptyUserAnswers.setValue(AddActiveBorderTransportMeansYesNoPage, true)
          val userAnswersGen = arbitraryTransportMeansActiveAnswers(initialAnswers, index)
          forAll(arbitrary[Mode], userAnswersGen) {
            (mode, userAnswers) =>
              val viewModelProvider = new TransportMeansAnswersViewModelProvider()
              val result            = viewModelProvider.apply(userAnswers, mode)(messages)
              val section           = result.sections(3)
              section.sectionTitle.get mustEqual sectionTitle
              section.rows.size must be > 1
              section.rows.head.key.value mustEqual "Do you want to add identification for this vehicle?"
              section.addAnotherLink must not be defined
          }
        }
      }

    }
  }
}
