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
import pages.transportMeans.{AddBorderModeOfTransportYesNoPage, BorderModeOfTransportPage}
import pages.transportMeans.departure._
import models.transportMeans.departure.Identification
import org.scalacheck.Arbitrary.arbitrary

class TransportMeansDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {
  private val identification: Identification = arbitrary[Identification].sample.value
  "TransportMeansDomain" - {

    "when post-transition" - {
      val mockPhaseConfig = mock[PhaseConfig]
      when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

      "cannot be parsed from user answers" - {

        "when Transition" - {
          "and office of departure not in CL010" - {
            "security type is no security details" in {
              val userAnswers = emptyUserAnswers
                .setValue(OfficeOfDepartureInCL010Page, false)
                .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              forAll(arbitraryTransportMeansDepartureAnswers(userAnswers)) {
                userAnswers =>
                  val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
                    TransportMeansDomain.userAnswersReader(mockPhaseConfig)
                  ).run(userAnswers)
                  result.left.value.page mustBe AddBorderModeOfTransportYesNoPage
              }
            }

            "security type is details (1,2, or 3)" in {
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, true)
                .setValue(OfficeOfDepartureInCL010Page, false)
                .setValue(SecurityDetailsTypePage, EntrySummaryDeclarationSecurityDetails)

              val result: EitherType[TransportMeansDomain] = UserAnswersReader[TransportMeansDomain](
                TransportMeansDomain.userAnswersReader(mockPhaseConfig)
              ).run(userAnswers)

              result.left.value.page mustBe BorderModeOfTransportPage
            }
          }
        }
      }
    }

  }
}
