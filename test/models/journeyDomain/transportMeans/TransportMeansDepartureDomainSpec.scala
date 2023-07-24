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
import models.Phase
import models.domain.{EitherType, UserAnswersReader}
import models.reference.Nationality
import models.transportMeans.InlandMode
import models.transportMeans.departure.Identification
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.preRequisites.ContainerIndicatorPage
import pages.transportMeans.InlandModePage
import pages.transportMeans.departure._

class TransportMeansDepartureDomainSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "TransportMeansDepartureDomain" - {

    val identification: Identification = arbitrary[Identification].sample.value
    val identificationNumber: String   = Gen.alphaNumStr.sample.value
    val nationality: Nationality       = arbitrary[Nationality].sample.value

    "can be parsed from user answers" - {

      "when post-transition" - {
        val mockPhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

        "when all questions are answered" in {
          val userAnswers = emptyUserAnswers
            .setValue(IdentificationPage, identification)
            .setValue(MeansIdentificationNumberPage, identificationNumber)
            .setValue(VehicleCountryPage, nationality)

          val expectedResult = PostTransitionTransportMeansDepartureDomain(
            identification = identification,
            identificationNumber = identificationNumber,
            nationality = nationality
          )

          val result: EitherType[TransportMeansDepartureDomain] = UserAnswersReader[TransportMeansDepartureDomain](
            TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig)
          ).run(userAnswers)

          result.value mustBe expectedResult
        }
      }
    }

    "can not be parsed from user answers" - {

      "when post-transition" - {
        val mockPhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

        "when identification page is missing" in {
          val result: EitherType[TransportMeansDepartureDomain] = UserAnswersReader[TransportMeansDepartureDomain](
            TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig)
          ).run(emptyUserAnswers)

          result.left.value.page mustBe IdentificationPage
        }

        "when identification number page is missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(IdentificationPage, identification)

          val result: EitherType[TransportMeansDepartureDomain] = UserAnswersReader[TransportMeansDepartureDomain](
            TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig)
          ).run(userAnswers)

          result.left.value.page mustBe MeansIdentificationNumberPage
        }

        "when vehicle country page is missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(IdentificationPage, identification)
            .setValue(MeansIdentificationNumberPage, identificationNumber)

          val result: EitherType[TransportMeansDepartureDomain] = UserAnswersReader[TransportMeansDepartureDomain](
            TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig)
          ).run(userAnswers)

          result.left.value.page mustBe VehicleCountryPage
        }
      }

      "when during transition" - {
        val mockPhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

        "and container indicator is 1" - {
          "and add identification type yes/no page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, true)

            val result: EitherType[TransportMeansDepartureDomain] = UserAnswersReader[TransportMeansDepartureDomain](
              TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe AddIdentificationTypeYesNoPage
          }

          "and identification page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, true)
              .setValue(AddIdentificationTypeYesNoPage, true)

            val result: EitherType[TransportMeansDepartureDomain] = UserAnswersReader[TransportMeansDepartureDomain](
              TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe IdentificationPage
          }

          "and add identification number yes/no page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, true)
              .setValue(AddIdentificationTypeYesNoPage, false)

            val result: EitherType[TransportMeansDepartureDomain] = UserAnswersReader[TransportMeansDepartureDomain](
              TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe AddIdentificationNumberYesNoPage
          }

          "and identification number page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, true)
              .setValue(AddIdentificationTypeYesNoPage, false)
              .setValue(AddIdentificationNumberYesNoPage, true)

            val result: EitherType[TransportMeansDepartureDomain] = UserAnswersReader[TransportMeansDepartureDomain](
              TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe MeansIdentificationNumberPage
          }

          "and add nationality yes/no page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, true)
              .setValue(InlandModePage, arbitrary[InlandMode](arbitraryNonRailInlandMode).sample.value)
              .setValue(AddIdentificationTypeYesNoPage, false)
              .setValue(AddIdentificationNumberYesNoPage, false)

            val result: EitherType[TransportMeansDepartureDomain] = UserAnswersReader[TransportMeansDepartureDomain](
              TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe AddVehicleCountryYesNoPage
          }

          "and nationality page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, true)
              .setValue(InlandModePage, arbitrary[InlandMode](arbitraryNonRailInlandMode).sample.value)
              .setValue(AddIdentificationTypeYesNoPage, false)
              .setValue(AddIdentificationNumberYesNoPage, false)
              .setValue(AddVehicleCountryYesNoPage, true)

            val result: EitherType[TransportMeansDepartureDomain] = UserAnswersReader[TransportMeansDepartureDomain](
              TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe VehicleCountryPage
          }
        }

        "and container indicator is 0" - {
          "and identification type page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, false)

            val result: EitherType[TransportMeansDepartureDomain] = UserAnswersReader[TransportMeansDepartureDomain](
              TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe IdentificationPage
          }

          "and identification number page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, false)
              .setValue(IdentificationPage, arbitrary[Identification].sample.value)

            val result: EitherType[TransportMeansDepartureDomain] = UserAnswersReader[TransportMeansDepartureDomain](
              TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe MeansIdentificationNumberPage
          }

          "and nationality page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, false)
              .setValue(InlandModePage, arbitrary[InlandMode](arbitraryNonRailInlandMode).sample.value)
              .setValue(IdentificationPage, arbitrary[Identification].sample.value)
              .setValue(MeansIdentificationNumberPage, Gen.alphaNumStr.sample.value)

            val result: EitherType[TransportMeansDepartureDomain] = UserAnswersReader[TransportMeansDepartureDomain](
              TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe VehicleCountryPage
          }
        }
      }
    }
  }
}
