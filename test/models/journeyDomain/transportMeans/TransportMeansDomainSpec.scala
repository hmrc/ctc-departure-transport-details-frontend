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
import pages.preRequisites.ContainerIndicatorPage
import pages.transportMeans.{
  AddActiveBorderTransportMeansYesNoPage,
  AddBorderModeOfTransportYesNoPage,
  AddDepartureTransportMeansYesNoPage,
  BorderModeOfTransportPage
}

class TransportMeansDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  "TransportMeansDomain" - {
    val mockPostTransitionPhaseConfig = mock[PhaseConfig]
    when(mockPostTransitionPhaseConfig.phase).thenReturn(Phase.PostTransition)

    val mockTransitionPhaseConfig = mock[PhaseConfig]
    when(mockTransitionPhaseConfig.phase).thenReturn(Phase.Transition)

    "transportMeansDepartureReader" - {
      "when in transition" - {
        "and container indicator is 1" - {
          "and add departures transport means yes/no is unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, true)

            val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
              TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe AddDepartureTransportMeansYesNoPage
          }

          "and add departures transport means yes/no is yes" - {
            "and add type of identification yes/no is unanswered" in {
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, true)
                .setValue(AddDepartureTransportMeansYesNoPage, true)

              val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
                TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig)
              ).run(userAnswers)

              result.left.value.page mustBe pages.transportMeans.departure.AddIdentificationTypeYesNoPage
            }
          }
        }

        "and container indicator is 0" - {
          "and type of identification is unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, false)
              .setValue(AddDepartureTransportMeansYesNoPage, true)

            val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
              TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe pages.transportMeans.departure.IdentificationPage
          }
        }
      }
    }

    "borderModeOfTransportReader" - {
      "when in post transition" - {
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
        "and office of departure not in CL010" - {
          "security type is 0" in {
            val userAnswers = emptyUserAnswers
              .setValue(OfficeOfDepartureInCL010Page, false)
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(AddDepartureTransportMeansYesNoPage, true)

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
              .setValue(OfficeOfDepartureInCL010Page, false)
              .setValue(SecurityDetailsTypePage, EntrySummaryDeclarationSecurityDetails)
              .setValue(AddDepartureTransportMeansYesNoPage, true)

            forAll(arbitraryTransportMeansDepartureAnswers(userAnswers)(mockTransitionPhaseConfig)) {
              userAnswers =>
                val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
                  TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig)
                ).run(userAnswers)
                result.left.value.page mustBe BorderModeOfTransportPage
            }
          }
        }

        "and office of departure is in CL010" - {
          "security type is 0" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(OfficeOfDepartureInCL010Page, true)
              .setValue(AddDepartureTransportMeansYesNoPage, true)

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
              .setValue(OfficeOfDepartureInCL010Page, true)
              .setValue(AddDepartureTransportMeansYesNoPage, true)

            forAll(arbitraryTransportMeansDepartureAnswers(userAnswers)(mockTransitionPhaseConfig)) {
              userAnswers =>
                val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
                  TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig)
                ).run(userAnswers)
                result.left.value.page mustBe AddBorderModeOfTransportYesNoPage
            }
          }
        }
      }
    }
  }
}
