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
import models.SecurityDetailsType.NoSecurityDetails
import models.domain.UserAnswersReader
import models.transportMeans.BorderModeOfTransport
import models.transportMeans.BorderModeOfTransport._
import models.{Index, Phase, SecurityDetailsType}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.SecurityDetailsTypePage
import pages.preRequisites.ContainerIndicatorPage
import pages.transportMeans.departure._
import pages.transportMeans.{active, AddBorderModeOfTransportYesNoPage, BorderModeOfTransportPage}

class TransportMeansDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "TransportMeansDomain" - {

    "transportMeansDepartureReader" - {
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

    "borderModeOfTransportReader" - {
      "when no security" - {
        "and add border mode of transport yes/no is missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)

          val result = UserAnswersReader[Option[BorderModeOfTransport]](
            TransportMeansDomain.borderModeOfTransportReader
          ).run(userAnswers)

          result.left.value.page mustBe AddBorderModeOfTransportYesNoPage
        }

        "and border mode is missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(AddBorderModeOfTransportYesNoPage, true)

          val result = UserAnswersReader[Option[BorderModeOfTransport]](
            TransportMeansDomain.borderModeOfTransportReader
          ).run(userAnswers)

          result.left.value.page mustBe BorderModeOfTransportPage
        }
      }

      "when there is security" - {
        "and border mode is missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, arbitrary[SecurityDetailsType](arbitrarySomeSecurityDetailsType).sample.value)

          val result = UserAnswersReader[Option[BorderModeOfTransport]](
            TransportMeansDomain.borderModeOfTransportReader
          ).run(userAnswers)

          result.left.value.page mustBe BorderModeOfTransportPage
        }
      }
    }

    "transportMeansActiveReader" - {
      "when no active border means answered" in {
        val result = UserAnswersReader[TransportMeansActiveListDomain](
          TransportMeansDomain.transportMeansActiveReader
        ).run(emptyUserAnswers)

        result.left.value.page mustBe active.IdentificationPage(Index(0))
      }
    }
  }
}
