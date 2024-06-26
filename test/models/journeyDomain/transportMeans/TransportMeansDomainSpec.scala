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
import config.PhaseConfig
import generators.Generators
import models.reference.BorderMode
import models.{OptionalBoolean, Phase}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.{OfficeOfDepartureInCL010Page, SecurityDetailsTypePage}
import pages.preRequisites.ContainerIndicatorPage
import pages.transportMeans._
import pages.transportMeans.active.{IdentificationPage, NationalityPage}

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
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

            val result = TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe AddDepartureTransportMeansYesNoPage
            result.left.value.pages mustBe Seq(
              AddDepartureTransportMeansYesNoPage
            )
          }

          "and add departures transport means yes/no is yes" - {
            "and add type of identification yes/no is unanswered" in {
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
                .setValue(AddDepartureTransportMeansYesNoPage, true)

              val result = TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(userAnswers)

              result.left.value.page mustBe departure.AddIdentificationTypeYesNoPage(departureIndex)
              result.left.value.pages mustBe Seq(
                AddDepartureTransportMeansYesNoPage,
                departure.AddIdentificationTypeYesNoPage(departureIndex)
              )
            }
          }
        }

        "and container indicator is 0" - {
          "and type of identification is unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.no)

            val result = TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe departure.IdentificationPage(departureIndex)
            result.left.value.pages mustBe Seq(
              departure.IdentificationPage(departureIndex)
            )
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

            forAll(arbitraryTransportMeansDepartureAnswers(userAnswers, departureIndex)(mockPostTransitionPhaseConfig)) {
              userAnswers =>
                val result = TransportMeansDomain.userAnswersReader(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

                result.left.value.page mustBe AddBorderModeOfTransportYesNoPage
            }
          }

          "security type is details (1,2,3)" in {
            val securityGen = arbitrary[String](arbitrarySomeSecurityDetailsType)
            forAll(securityGen) {
              securityType =>
                val userAnswers = emptyUserAnswers
                  .setValue(SecurityDetailsTypePage, securityType)

                forAll(arbitraryTransportMeansDepartureAnswers(userAnswers, departureIndex)(mockPostTransitionPhaseConfig)) {
                  userAnswers =>
                    val result = TransportMeansDomain.userAnswersReader(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

                    result.left.value.page mustBe BorderModeOfTransportPage
                }
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

            forAll(arbitraryTransportMeansDepartureAnswers(userAnswers, departureIndex)(mockTransitionPhaseConfig)) {
              userAnswers =>
                val result = TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(userAnswers)

                result.left.value.page mustBe AddBorderModeOfTransportYesNoPage
            }
          }

          "security type is details (1,2,3)" in {
            val securityGen = arbitrary[String](arbitrarySomeSecurityDetailsType)
            forAll(securityGen) {
              securityType =>
                val userAnswers = emptyUserAnswers
                  .setValue(OfficeOfDepartureInCL010Page, false)
                  .setValue(SecurityDetailsTypePage, securityType)
                  .setValue(AddDepartureTransportMeansYesNoPage, true)

                forAll(arbitraryTransportMeansDepartureAnswers(userAnswers, departureIndex)(mockTransitionPhaseConfig)) {
                  userAnswers =>
                    val result = TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(userAnswers)

                    result.left.value.page mustBe BorderModeOfTransportPage
                }
            }
          }
        }

        "and office of departure is in CL010" - {
          "security type is 0" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(OfficeOfDepartureInCL010Page, true)
              .setValue(AddDepartureTransportMeansYesNoPage, true)

            forAll(arbitraryTransportMeansDepartureAnswers(userAnswers, departureIndex)(mockTransitionPhaseConfig)) {
              userAnswers =>
                val result = TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(userAnswers)

                result.left.value.page mustBe AddBorderModeOfTransportYesNoPage
            }
          }

          "security type is details (1,2,3)" in {
            val securityGen = arbitrary[String](arbitrarySomeSecurityDetailsType)
            forAll(securityGen) {
              securityType =>
                val userAnswers = emptyUserAnswers
                  .setValue(SecurityDetailsTypePage, securityType)
                  .setValue(OfficeOfDepartureInCL010Page, true)
                  .setValue(AddDepartureTransportMeansYesNoPage, true)

                forAll(arbitraryTransportMeansDepartureAnswers(userAnswers, departureIndex)(mockTransitionPhaseConfig)) {
                  userAnswers =>
                    val result = TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(userAnswers)

                    result.left.value.page mustBe AddBorderModeOfTransportYesNoPage
                }
            }
          }
        }
      }
    }

    "transportMeansActiveReader" - {
      "when in transition" - {
        "borderModeOfTransport present" - {
          "and not type ChannelTunnel" in {
            val securityGen   = arbitrary[String](arbitrarySomeSecurityDetailsType)
            val borderModeGen = arbitrary[Option[BorderMode]](arbitraryOptionalNonRailBorderModeOfTransport)
            forAll(securityGen, borderModeGen) {
              (securityType, borderMode) =>
                val userAnswers = emptyUserAnswers
                  .setValue(OfficeOfDepartureInCL010Page, false)
                  .setValue(SecurityDetailsTypePage, securityType)
                  .setValue(AddDepartureTransportMeansYesNoPage, true)
                  .setValue(BorderModeOfTransportPage, borderMode)

                forAll(arbitraryTransportMeansDepartureAnswers(userAnswers, departureIndex)(mockTransitionPhaseConfig)) {
                  userAnswers =>
                    val result = TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(userAnswers)

                    result.left.value.page mustBe NationalityPage(index)
                }
            }
          }

          "and type ChannelTunnel" in {
            val securityGen = arbitrary[String](arbitrarySomeSecurityDetailsType)
            forAll(securityGen) {
              securityType =>
                val userAnswers = emptyUserAnswers
                  .setValue(OfficeOfDepartureInCL010Page, false)
                  .setValue(SecurityDetailsTypePage, securityType)
                  .setValue(AddDepartureTransportMeansYesNoPage, true)
                  .setValue(BorderModeOfTransportPage, BorderMode("2", "Rail"))

                forAll(arbitraryTransportMeansDepartureAnswers(userAnswers, departureIndex)(mockTransitionPhaseConfig)) {
                  userAnswers =>
                    val result = TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(userAnswers)

                    result.left.value.page mustBe AddActiveBorderTransportMeansYesNoPage
                }
            }
          }
        }

        "borderModeOfTransport is not present" in {
          val securityGen = arbitrary[String](arbitrarySomeSecurityDetailsType)
          forAll(securityGen) {
            securityType =>
              val userAnswers = emptyUserAnswers
                .setValue(OfficeOfDepartureInCL010Page, true)
                .setValue(SecurityDetailsTypePage, securityType)
                .setValue(AddDepartureTransportMeansYesNoPage, true)
                .setValue(AddBorderModeOfTransportYesNoPage, false)

              forAll(arbitraryTransportMeansDepartureAnswers(userAnswers, departureIndex)(mockTransitionPhaseConfig)) {
                userAnswers =>
                  val result = TransportMeansDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(userAnswers)

                  result.left.value.page mustBe AddActiveBorderTransportMeansYesNoPage
              }
          }
        }
      }

      "when in postTransition" - {

        "securityDetails is type NoSecurityDetails" in {

          val userAnswers = emptyUserAnswers
            .setValue(AddDepartureTransportMeansYesNoPage, true)
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(AddBorderModeOfTransportYesNoPage, false)

          forAll(arbitraryTransportMeansDepartureAnswers(userAnswers, departureIndex)(mockPostTransitionPhaseConfig)) {
            userAnswers =>
              val result = TransportMeansDomain.userAnswersReader(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

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

              forAll(arbitraryTransportMeansDepartureAnswers(userAnswers, departureIndex)(mockPostTransitionPhaseConfig)) {
                userAnswers =>
                  val result = TransportMeansDomain.userAnswersReader(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

                  result.left.value.page mustBe IdentificationPage(index)
              }
          }
        }
      }
    }
  }
}
