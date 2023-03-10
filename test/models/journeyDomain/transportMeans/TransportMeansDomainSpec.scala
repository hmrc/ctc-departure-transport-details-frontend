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
import models.transportMeans.BorderModeOfTransport
import models.transportMeans.BorderModeOfTransport._
import models.transportMeans.departure.{InlandMode, Identification => DepartureIdentification}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.departure.InlandModePage
import pages.transportMeans.{active, departure, BorderModeOfTransportPage}

class TransportMeansDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "TransportMeansDomain" - {
    "can be parsed from user answers" - {
      "when inland mode is 5 (mail)" in {
        val inlandMode = InlandMode.Mail

        val answers = emptyUserAnswers.setValue(InlandModePage, inlandMode)

        val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
          TransportMeansDomain.userAnswersReader
        ).run(answers)

        result.value.inlandMode mustBe inlandMode
      }

      "when inland mode is not 5 (mail)" - {
        forAll(arbitrary[InlandMode](arbitraryNonMailInlandMode), arbitrary[BorderModeOfTransport]) {
          (inlandMode, borderModeOfTransport) =>
            val initialAnswers = emptyUserAnswers
              .setValue(InlandModePage, inlandMode)
              .setValue(BorderModeOfTransportPage, borderModeOfTransport)

            forAll(arbitraryTransportMeansAnswers(initialAnswers)) {
              answers =>
                val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
                  TransportMeansDomain.userAnswersReader
                ).run(answers)

                result.value.inlandMode mustBe inlandMode
            }
        }
      }
    }

    "cannot be parsed from user answers" - {
      "when inland mode is unanswered" in {
        val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
          TransportMeansDomain.userAnswersReader
        ).run(emptyUserAnswers)

        result.left.value.page mustBe InlandModePage
      }

      "when border mode of transport is unanswered" in {
        forAll(arbitrary[InlandMode](arbitraryNonMailInlandMode)) {
          inlandMode =>
            val userAnswers = emptyUserAnswers
              .setValue(InlandModePage, inlandMode)
              .setValue(departure.IdentificationPage, arbitrary[DepartureIdentification].sample.value)
              .setValue(departure.MeansIdentificationNumberPage, nonEmptyString.sample.value)
              .setValue(departure.VehicleCountryPage, arbitrary[Nationality].sample.value)

            val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
              TransportMeansDomain.userAnswersReader
            ).run(userAnswers)

            result.left.value.page mustBe BorderModeOfTransportPage
        }
      }

      "when no active border means answered" - {
        "and Sea/Air border mode" in {
          forAll(arbitrary[InlandMode](arbitraryNonMailInlandMode), Gen.oneOf(Sea, Air)) {
            (inlandMode, borderMode) =>
              val userAnswers = emptyUserAnswers
                .setValue(InlandModePage, inlandMode)
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

        "and ChannelTunnel/IrishLandBoundary border mode" in {
          forAll(arbitrary[InlandMode](arbitraryNonMailInlandMode), Gen.oneOf(ChannelTunnel, IrishLandBoundary)) {
            (inlandMode, borderMode) =>
              val userAnswers = emptyUserAnswers
                .setValue(InlandModePage, inlandMode)
                .setValue(departure.IdentificationPage, arbitrary[DepartureIdentification].sample.value)
                .setValue(departure.MeansIdentificationNumberPage, nonEmptyString.sample.value)
                .setValue(departure.VehicleCountryPage, arbitrary[Nationality].sample.value)
                .setValue(BorderModeOfTransportPage, borderMode)

              val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
                TransportMeansDomain.userAnswersReader
              ).run(userAnswers)

              result.left.value.page mustBe active.IdentificationNumberPage(Index(0))
          }
        }
      }
    }
  }
}
