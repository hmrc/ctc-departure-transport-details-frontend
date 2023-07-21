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
import pages.transportMeans._

class TransportMeansDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "TransportMeansDomain" - {

    "when post-transition" - {
      val mockPhaseConfig = mock[PhaseConfig]
      when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)


  }

    "can be parsed from user answers" - {

      "when Transition" - {
      }
      }


      "and container indicator is 1" - {
        "and add departures transport means yes/no is unanswered" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIndicatorPage, true)

          val result = UserAnswersReader[Option[TransportMeansDepartureDomain]](
            TransportMeansDomain.transportMeansDepartureReader(mockPhaseConfig)
          ).run(userAnswers)

          result.left.value.page mustBe AddDepartureTransportMeansYesNoPage
        }

      }
}
