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
import models.Index
import models.domain.{EitherType, UserAnswersReader}
import models.reference.Nationality
import models.transportMeans.BorderModeOfTransport._
import models.transportMeans.departure.{Identification => DepartureIdentification}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.{active, departure, BorderModeOfTransportPage}

class TransportMeansDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "TransportMeansDomain" - {
    "can be parsed from user answers" - {}

    "cannot be parsed from user answers" - {

      "when border mode of transport is unanswered" in {
        val userAnswers = emptyUserAnswers
          .setValue(departure.IdentificationPage, arbitrary[DepartureIdentification].sample.value)
          .setValue(departure.MeansIdentificationNumberPage, nonEmptyString.sample.value)
          .setValue(departure.VehicleCountryPage, arbitrary[Nationality].sample.value)

        val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
          TransportMeansDomain.userAnswersReader
        ).run(userAnswers)

        result.left.value.page mustBe BorderModeOfTransportPage
      }

      "when no active border means answered" in {
        forAll(Gen.oneOf(Sea, Air)) {
          borderMode =>
            val userAnswers = emptyUserAnswers
              .setValue(departure.IdentificationPage, arbitrary[DepartureIdentification].sample.value)
              .setValue(departure.MeansIdentificationNumberPage, nonEmptyString.sample.value)
              .setValue(departure.VehicleCountryPage, arbitrary[Nationality].sample.value)
              .setValue(BorderModeOfTransportPage, borderMode)

            val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
              TransportMeansDomain.userAnswersReader
            ).run(userAnswers)

            result.left.value.page mustBe active.IdentificationPage(Index(0))
        }
      }
    }
  }
}
