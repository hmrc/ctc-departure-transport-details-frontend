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
import models.SecurityDetailsType.{EntrySummaryDeclarationSecurityDetails, NoSecurityDetails}
import models.domain.{EitherType, UserAnswersReader}
import models.transportMeans.BorderModeOfTransport._
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.{OfficeOfDepartureInCL010Page, SecurityDetailsTypePage}
import pages.transportMeans.departure.AddVehicleIdentificationYesNoPage
import pages.transportMeans.{AddBorderModeOfTransportYesNoPage, BorderModeOfTransportPage}

class TransportMeansDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "TransportMeansDomain" - {

    "cannot be parsed from user answers" - {

      "when in post transition" - {
        val mockPostTransitionPhaseConfig: PhaseConfig = mock[PhaseConfig]
        when(mockPostTransitionPhaseConfig.phase).thenReturn(Phase.PostTransition)
        "and office of departure not in CL010" - {
          "security type is 0" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            forAll(arbitraryTransportMeansDepartureAnswers(userAnswers)(mockPostTransitionPhaseConfig)) {
              userAnswers =>
                val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
                  TransportMeansDomain.userAnswersReader(mockPostTransitionPhaseConfig)
                ).run(userAnswers)
                result.left.value.page mustBe AddBorderModeOfTransportYesNoPage
            }
          }

          "security type is details (1,2,3)" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, EntrySummaryDeclarationSecurityDetails)

            forAll(arbitraryTransportMeansDepartureAnswers(userAnswers)(mockPostTransitionPhaseConfig)) {
              userAnswers =>
                val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
                  TransportMeansDomain.userAnswersReader(mockPostTransitionPhaseConfig)
                ).run(userAnswers)
                result.left.value.page mustBe BorderModeOfTransportPage
            }
          }
        }
      }

      "when in transition" - {
        val mockTransitionPhaseConfig: PhaseConfig = mock[PhaseConfig]
        when(mockTransitionPhaseConfig.phase).thenReturn(Phase.Transition)
        "and office of departure not in CL010" - {
          "security type is 0" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(OfficeOfDepartureInCL010Page, false)
              .setValue(AddVehicleIdentificationYesNoPage, false)

            forAll(arbitraryTransportMeansDepartureAnswers(userAnswers)(mockTransitionPhaseConfig)) {
              userAnswers =>
                val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
                  TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig)
                ).run(userAnswers)
                result.left.value.page mustBe AddBorderModeOfTransportYesNoPage
            }
          }

          "security type is details (1,2,3)" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, EntrySummaryDeclarationSecurityDetails)
              .setValue(OfficeOfDepartureInCL010Page, false)
              .setValue(AddVehicleIdentificationYesNoPage, false)

            forAll(arbitraryTransportMeansDepartureAnswers(userAnswers)(mockTransitionPhaseConfig)) {
              userAnswers =>
                val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
                  TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig)
                ).run(userAnswers)
                result.left.value.page mustBe BorderModeOfTransportPage
            }
          }
        }
      }
    }
  }
}
