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
import config.PhaseConfig
import generators.Generators
import models.domain.{EitherType, UserAnswersReader}
import models.reference.Nationality
import models.transportMeans.BorderModeOfTransport._
import models.transportMeans.departure.{Identification => DepartureIdentification}
import models.{Index, Phase}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.preRequisites.ContainerIndicatorPage
import pages.transportMeans.departure._
import pages.transportMeans.{active, departure, BorderModeOfTransportPage}

class TransportMeansDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "TransportMeansDomain" - {
    "can be parsed from user answers" - {}

    "cannot be parsed from user answers" - {

      "when post-transition" - {
        val mockPhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

        "when border mode of transport is unanswered" in {
          val userAnswers = emptyUserAnswers
            .setValue(departure.IdentificationPage, arbitrary[DepartureIdentification].sample.value)
            .setValue(departure.MeansIdentificationNumberPage, nonEmptyString.sample.value)
            .setValue(departure.VehicleCountryPage, arbitrary[Nationality].sample.value)

          val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
            TransportMeansDomain.userAnswersReader(mockPhaseConfig)
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
                TransportMeansDomain.userAnswersReader(mockPhaseConfig)
              ).run(userAnswers)

              result.left.value.page mustBe active.IdentificationPage(Index(0))
          }
        }
      }

      "when during transition" - {
        val mockPhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

        "and container indicator is 1" - {
          "and add departures transport means yes/no is unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, true)

            val result = UserAnswersReader[Option[TransportMeansDepartureDomain]](
              TransportMeansDomain.transportMeansDepartureReader(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe AddVehicleIdentificationYesNoPage
          }

          "and add departures transport means yes/no is yes" - {
            "and add type of identification yes/no is unanswered" in {
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, true)
                .setValue(AddVehicleIdentificationYesNoPage, true)

              val result = UserAnswersReader[Option[TransportMeansDepartureDomain]](
                TransportMeansDomain.transportMeansDepartureReader(mockPhaseConfig)
              ).run(userAnswers)

              result.left.value.page mustBe AddIdentificationTypeYesNoPage
            }
          }
        }

        "and container indicator is 0" - {
          "and type of identification is unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, false)

            val result = UserAnswersReader[Option[TransportMeansDepartureDomain]](
              TransportMeansDomain.transportMeansDepartureReader(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe IdentificationPage
          }
        }
      }
    }
  }
}
