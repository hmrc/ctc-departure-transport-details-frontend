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
import config.Constants.SecurityType.NoSecurityDetails
import generators.Generators
import models.reference.BorderMode
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.SecurityDetailsTypePage
import pages.transportMeans.*
import pages.transportMeans.active.IdentificationPage

class TransportMeansDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "TransportMeansDomain" - {

    "borderModeOfTransportReader" - {
      "and office of departure not in CL010" - {
        "security type is 0" in {
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)

          forAll(arbitraryTransportMeansDepartureAnswers(userAnswers, departureIndex)) {
            userAnswers =>
              val result = TransportMeansDomain.userAnswersReader.apply(Nil).run(userAnswers)

              result.left.value.page mustBe AddBorderModeOfTransportYesNoPage
          }
        }

        "security type is details (1,2,3)" in {
          val securityGen = arbitrary[String](arbitrarySomeSecurityDetailsType)
          forAll(securityGen) {
            securityType =>
              val userAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, securityType)

              forAll(arbitraryTransportMeansDepartureAnswers(userAnswers, departureIndex)) {
                userAnswers =>
                  val result = TransportMeansDomain.userAnswersReader.apply(Nil).run(userAnswers)

                  result.left.value.page mustBe BorderModeOfTransportPage
              }
          }
        }
      }
    }

    "transportMeansActiveReader" - {

      "securityDetails is type NoSecurityDetails" in {

        val userAnswers = emptyUserAnswers
          .setValue(AddDepartureTransportMeansYesNoPage, true)
          .setValue(SecurityDetailsTypePage, NoSecurityDetails)
          .setValue(AddBorderModeOfTransportYesNoPage, false)

        forAll(arbitraryTransportMeansDepartureAnswers(userAnswers, departureIndex)) {
          userAnswers =>
            val result = TransportMeansDomain.userAnswersReader.apply(Nil).run(userAnswers)

            result.left.value.page mustBe AddActiveBorderTransportMeansYesNoPage
        }
      }

      "securityDetails is not type NoSecurityDetails" in {
        val securityGen = arbitrary[String](arbitrarySomeSecurityDetailsType)
        forAll(securityGen) {
          securityType =>
            val userAnswers = emptyUserAnswers
              .setValue(AddDepartureTransportMeansYesNoPage, true)
              .setValue(SecurityDetailsTypePage, securityType)
              .setValue(BorderModeOfTransportPage, arbitrary[BorderMode].sample.value)

            forAll(arbitraryTransportMeansDepartureAnswers(userAnswers, departureIndex)) {
              userAnswers =>
                val result = TransportMeansDomain.userAnswersReader.apply(Nil).run(userAnswers)

                result.left.value.page mustBe IdentificationPage(index)
            }
        }
      }
    }
  }
}
