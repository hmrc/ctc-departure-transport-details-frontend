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

package models.journeyDomain.transportMeans

import base.SpecBase
import generators.Generators
import models.reference.Nationality
import models.reference.transportMeans.departure.Identification
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.transportMeans.DepartureSection
import pages.transportMeans.departure.*

class TransportMeansDepartureDomainSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "TransportMeansDepartureDomain" - {

    val identification: Identification = arbitrary[Identification].sample.value
    val identificationNumber: String   = Gen.alphaNumStr.sample.value
    val nationality: Nationality       = arbitrary[Nationality].sample.value

    "can be parsed from user answers" - {

      "when all questions are answered" in {
        val userAnswers = emptyUserAnswers
          .setValue(IdentificationPage(departureIndex), identification)
          .setValue(MeansIdentificationNumberPage(departureIndex), identificationNumber)
          .setValue(VehicleCountryPage(departureIndex), nationality)

        val expectedResult = TransportMeansDepartureDomain(
          identification = identification,
          identificationNumber = identificationNumber,
          nationality = nationality
        )(departureIndex)

        val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex).apply(Nil).run(userAnswers)

        result.value.value mustBe expectedResult
        result.value.pages mustBe Seq(
          IdentificationPage(departureIndex),
          MeansIdentificationNumberPage(departureIndex),
          VehicleCountryPage(departureIndex),
          DepartureSection(departureIndex)
        )
      }
    }

    "can not be parsed from user answers" - {

      "when identification page is missing" in {

        val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex).apply(Nil).run(emptyUserAnswers)

        result.left.value.page mustBe IdentificationPage(departureIndex)
        result.left.value.pages mustBe Seq(
          IdentificationPage(departureIndex)
        )
      }

      "when identification number page is missing" in {
        val userAnswers = emptyUserAnswers
          .setValue(IdentificationPage(departureIndex), identification)

        val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex).apply(Nil).run(userAnswers)

        result.left.value.page mustBe MeansIdentificationNumberPage(departureIndex)
        result.left.value.pages mustBe Seq(
          IdentificationPage(departureIndex),
          MeansIdentificationNumberPage(departureIndex)
        )
      }

      "when vehicle country page is missing" in {
        val userAnswers = emptyUserAnswers
          .setValue(IdentificationPage(departureIndex), identification)
          .setValue(MeansIdentificationNumberPage(departureIndex), identificationNumber)

        val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex).apply(Nil).run(userAnswers)

        result.left.value.page mustBe VehicleCountryPage(departureIndex)
        result.left.value.pages mustBe Seq(
          IdentificationPage(departureIndex),
          MeansIdentificationNumberPage(departureIndex),
          VehicleCountryPage(departureIndex)
        )
      }
    }
  }
}
