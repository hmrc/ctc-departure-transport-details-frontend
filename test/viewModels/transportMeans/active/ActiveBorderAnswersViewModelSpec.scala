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

package viewModels.transportMeans.active

import base.SpecBase
import generators.Generators
import models.Mode
import models.SecurityDetailsType.{EntrySummaryDeclarationSecurityDetails, NoSecurityDetails}
import models.reference.{CustomsOffice, Nationality}
import models.transportMeans.BorderModeOfTransport.{Air, Sea}
import models.transportMeans.active.Identification
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.SecurityDetailsTypePage
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.active._
import viewModels.transportMeans.active.ActiveBorderAnswersViewModel.ActiveBorderAnswersViewModelProvider

class ActiveBorderAnswersViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val mode: Mode                   = arbitrary[Mode].sample.value
  val identificationNumber: String = Gen.alphaNumStr.sample.value
  val conveyanceNumber: String     = Gen.alphaNumStr.sample.value

  "must return 1 section" in {
    forAll(arbitraryTransportMeansActiveAnswers(emptyUserAnswers, activeIndex), arbitrary[Mode]) {
      (userAnswers, mode) =>
        val sections = new ActiveBorderAnswersViewModelProvider().apply(userAnswers, mode, activeIndex).sections
        sections.size mustBe 1
    }
  }

  "when add vehicle nationality is true" - {
    "and security type is either 1,2,3 and mode of transport is air" - {
      "must return 8 rows" in {
        forAll(arbitrary[Identification], arbitrary[CustomsOffice], arbitrary[Nationality]) {
          (identification, office, nationality) =>
            val answers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, EntrySummaryDeclarationSecurityDetails)
              .setValue(BorderModeOfTransportPage, Air)
              .setValue(IdentificationPage(activeIndex), identification)
              .setValue(IdentificationNumberPage(activeIndex), identificationNumber)
              .setValue(AddNationalityYesNoPage(activeIndex), true)
              .setValue(NationalityPage(activeIndex), nationality)
              .setValue(CustomsOfficeActiveBorderPage(activeIndex), office)
              .setValue(ConveyanceReferenceNumberPage(activeIndex), conveyanceNumber)
              .setValue(AddIdentificationYesNoPage(activeIndex), true)
              .setValue(AddVehicleIdentificationNumberYesNoPage(activeIndex), true)

            val viewModelProvider   = injector.instanceOf[ActiveBorderAnswersViewModelProvider]
            val sections            = viewModelProvider.apply(answers, mode, activeIndex).sections
            val activeBorderSection = sections.head

            activeBorderSection.sectionTitle mustNot be(defined)
            activeBorderSection.rows.size mustBe 8
            activeBorderSection.addAnotherLink must not be defined

        }
      }
    }

    "and security type is 0 and mode of transport is sea" - {
      "must return 9 rows" in {
        forAll(arbitrary[Identification], arbitrary[CustomsOffice], arbitrary[Nationality]) {
          (identification, office, nationality) =>
            val answers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(BorderModeOfTransportPage, Sea)
              .setValue(IdentificationPage(activeIndex), identification)
              .setValue(IdentificationNumberPage(activeIndex), identificationNumber)
              .setValue(AddNationalityYesNoPage(activeIndex), true)
              .setValue(NationalityPage(activeIndex), nationality)
              .setValue(CustomsOfficeActiveBorderPage(activeIndex), office)
              .setValue(ConveyanceReferenceNumberYesNoPage(activeIndex), true)
              .setValue(ConveyanceReferenceNumberPage(activeIndex), conveyanceNumber)
              .setValue(AddIdentificationYesNoPage(activeIndex), true)
              .setValue(AddVehicleIdentificationNumberYesNoPage(activeIndex), true)

            val viewModelProvider   = injector.instanceOf[ActiveBorderAnswersViewModelProvider]
            val sections            = viewModelProvider.apply(answers, mode, activeIndex).sections
            val activeBorderSection = sections.head

            activeBorderSection.sectionTitle mustNot be(defined)
            activeBorderSection.rows.size mustBe 9
            activeBorderSection.addAnotherLink must not be defined

        }

      }
    }
  }

  "when add vehicle nationality is false" - {
    "and security type is either 1,2,3 and mode of transport is air" - {
      "must return 8 rows" in {
        forAll(arbitrary[Identification], arbitrary[CustomsOffice]) {
          (identification, office) =>
            val answers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(BorderModeOfTransportPage, Sea)
              .setValue(IdentificationPage(activeIndex), identification)
              .setValue(IdentificationNumberPage(activeIndex), identificationNumber)
              .setValue(AddNationalityYesNoPage(activeIndex), false)
              .setValue(CustomsOfficeActiveBorderPage(activeIndex), office)
              .setValue(ConveyanceReferenceNumberYesNoPage(activeIndex), true)
              .setValue(ConveyanceReferenceNumberPage(activeIndex), conveyanceNumber)
              .setValue(AddIdentificationYesNoPage(activeIndex), true)
              .setValue(AddVehicleIdentificationNumberYesNoPage(activeIndex), true)

            val viewModelProvider   = injector.instanceOf[ActiveBorderAnswersViewModelProvider]
            val sections            = viewModelProvider.apply(answers, mode, activeIndex).sections
            val activeBorderSection = sections.head

            activeBorderSection.sectionTitle mustNot be(defined)
            activeBorderSection.rows.size mustBe 8
            activeBorderSection.addAnotherLink must not be defined

        }
      }

    }

    "and security type is either 0 and mode of transport is maritime" - {
      "must return 8 rows" in {
        forAll(arbitrary[Identification], arbitrary[CustomsOffice]) {
          (identification, office) =>
            val answers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(BorderModeOfTransportPage, Sea)
              .setValue(IdentificationPage(activeIndex), identification)
              .setValue(IdentificationNumberPage(activeIndex), identificationNumber)
              .setValue(AddNationalityYesNoPage(activeIndex), false)
              .setValue(CustomsOfficeActiveBorderPage(activeIndex), office)
              .setValue(ConveyanceReferenceNumberYesNoPage(activeIndex), true)
              .setValue(ConveyanceReferenceNumberPage(activeIndex), conveyanceNumber)
              .setValue(AddIdentificationYesNoPage(activeIndex), true)
              .setValue(AddVehicleIdentificationNumberYesNoPage(activeIndex), true)

            val viewModelProvider   = injector.instanceOf[ActiveBorderAnswersViewModelProvider]
            val sections            = viewModelProvider.apply(answers, mode, activeIndex).sections
            val activeBorderSection = sections.head

            activeBorderSection.sectionTitle mustNot be(defined)
            activeBorderSection.rows.size mustBe 8
            activeBorderSection.addAnotherLink must not be defined
        }
      }
    }
  }
}
