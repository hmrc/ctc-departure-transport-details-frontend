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
import models.SecurityDetailsType.{EntrySummaryDeclarationSecurityDetails, NoSecurityDetails}
import models.domain.{EitherType, UserAnswersReader}
import models.reference.{CustomsOffice, Nationality}
import models.transportMeans.BorderModeOfTransport
import models.transportMeans.BorderModeOfTransport._
import models.transportMeans.active.Identification
import models.transportMeans.active.Identification._
import models.{Phase, SecurityDetailsType}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.SecurityDetailsTypePage
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.active._

class TransportMeansActiveDomainSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "TransportMeansActiveDomain" - {

    val mockPostTransitionPhaseConfig = mock[PhaseConfig]
    when(mockPostTransitionPhaseConfig.phase).thenReturn(Phase.PostTransition)

    val mockTransitionPhaseConfig = mock[PhaseConfig]
    when(mockTransitionPhaseConfig.phase).thenReturn(Phase.Transition)

    val identification: Identification = arbitrary[Identification].sample.value
    val identificationNumber: String   = Gen.alphaNumStr.sample.value
    val nationality: Nationality       = arbitrary[Nationality].sample.value
    val customsOffice: CustomsOffice   = arbitrary[CustomsOffice].sample.value
    val conveyanceNumber: String       = Gen.alphaNumStr.sample.value

    "in post-transition" - {
      "can be parsed from user answers" - {
        "when the add nationality is answered yes" - {
          "and security detail type is 0 and inland mode is Sea and add conveyance number is yes" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(BorderModeOfTransportPage, Sea)
              .setValue(IdentificationPage(index), identification)
              .setValue(IdentificationNumberPage(index), identificationNumber)
              .setValue(AddNationalityYesNoPage(index), true)
              .setValue(NationalityPage(index), nationality)
              .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)
              .setValue(ConveyanceReferenceNumberYesNoPage(index), true)
              .setValue(ConveyanceReferenceNumberPage(index), conveyanceNumber)

            val expectedResult = PostTransitionTransportMeansActiveDomain(
              identification = identification,
              identificationNumber = identificationNumber,
              nationality = Option(nationality),
              customsOffice = customsOffice,
              conveyanceReferenceNumber = Some(conveyanceNumber)
            )(index)

            val result: EitherType[TransportMeansActiveDomain] = UserAnswersReader[TransportMeansActiveDomain](
              TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig)
            ).run(userAnswers)

            result.value mustBe expectedResult
          }

          "and security detail type is 1 and inland mode is Air" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, EntrySummaryDeclarationSecurityDetails)
              .setValue(BorderModeOfTransportPage, Air)
              .setValue(IdentificationPage(index), identification)
              .setValue(IdentificationNumberPage(index), identificationNumber)
              .setValue(AddNationalityYesNoPage(index), true)
              .setValue(NationalityPage(index), nationality)
              .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)
              .setValue(ConveyanceReferenceNumberPage(index), conveyanceNumber)

            val expectedResult = PostTransitionTransportMeansActiveDomain(
              identification = identification,
              identificationNumber = identificationNumber,
              nationality = Option(nationality),
              customsOffice = customsOffice,
              conveyanceReferenceNumber = Some(conveyanceNumber)
            )(index)

            val result: EitherType[TransportMeansActiveDomain] = UserAnswersReader[TransportMeansActiveDomain](
              TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig)
            ).run(userAnswers)

            result.value mustBe expectedResult
          }
        }

        "when the add nationality is answered no" - {
          "and security detail type is 0 and inland mode is Sea and add conveyance number is no" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(BorderModeOfTransportPage, Sea)
              .setValue(IdentificationPage(index), identification)
              .setValue(IdentificationNumberPage(index), identificationNumber)
              .setValue(AddNationalityYesNoPage(index), false)
              .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)
              .setValue(ConveyanceReferenceNumberYesNoPage(index), false)

            val expectedResult = PostTransitionTransportMeansActiveDomain(
              identification = identification,
              identificationNumber = identificationNumber,
              nationality = None,
              customsOffice = customsOffice,
              conveyanceReferenceNumber = None
            )(index)

            val result: EitherType[TransportMeansActiveDomain] = UserAnswersReader[TransportMeansActiveDomain](
              TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig)
            ).run(userAnswers)

            result.value mustBe expectedResult
          }
        }
      }

      "can not be parsed from user answers" - {
        "when border mode is answered" - {
          "must go to identification type" in {
            forAll(arbitrary[BorderModeOfTransport]) {
              borderModeOfTransport =>
                val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, borderModeOfTransport)

                val result: EitherType[TransportMeansActiveDomain] = UserAnswersReader[TransportMeansActiveDomain](
                  TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig)
                ).run(userAnswers)

                result.left.value.page mustBe IdentificationPage(index)
            }
          }
        }

        "when add nationality is unanswered" in {
          val userAnswers = emptyUserAnswers
            .setValue(BorderModeOfTransportPage, BorderModeOfTransport.IrishLandBoundary)
            .setValue(InferredIdentificationPage(index), Identification.RegNumberRoadVehicle)
            .setValue(IdentificationNumberPage(index), identificationNumber)

          val result: EitherType[TransportMeansActiveDomain] = UserAnswersReader[TransportMeansActiveDomain](
            TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig)
          ).run(userAnswers)

          result.left.value.page mustBe AddNationalityYesNoPage(index)
        }

        "when nationality is unanswered" in {
          val userAnswers = emptyUserAnswers
            .setValue(BorderModeOfTransportPage, BorderModeOfTransport.Air)
            .setValue(IdentificationPage(index), Identification.IataFlightNumber)
            .setValue(IdentificationNumberPage(index), identificationNumber)
            .setValue(AddNationalityYesNoPage(index), true)

          val result: EitherType[TransportMeansActiveDomain] = UserAnswersReader[TransportMeansActiveDomain](
            TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig)
          ).run(userAnswers)

          result.left.value.page mustBe NationalityPage(index)
        }

        "when customs office ref. number is unanswered" - {
          "and add nationality is true" in {
            val userAnswers = emptyUserAnswers
              .setValue(BorderModeOfTransportPage, BorderModeOfTransport.IrishLandBoundary)
              .setValue(InferredIdentificationPage(index), Identification.RegNumberRoadVehicle)
              .setValue(IdentificationNumberPage(index), identificationNumber)
              .setValue(AddNationalityYesNoPage(index), true)
              .setValue(NationalityPage(index), nationality)

            val result: EitherType[TransportMeansActiveDomain] = UserAnswersReader[TransportMeansActiveDomain](
              TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe CustomsOfficeActiveBorderPage(index)
          }

          "and add nationality is false" in {
            val userAnswers = emptyUserAnswers
              .setValue(BorderModeOfTransportPage, BorderModeOfTransport.IrishLandBoundary)
              .setValue(InferredIdentificationPage(index), Identification.RegNumberRoadVehicle)
              .setValue(IdentificationNumberPage(index), identificationNumber)
              .setValue(AddNationalityYesNoPage(index), false)

            val result: EitherType[TransportMeansActiveDomain] = UserAnswersReader[TransportMeansActiveDomain](
              TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe CustomsOfficeActiveBorderPage(index)
          }
        }

        "when security is in set {1,2,3}" - {
          "and border mode of transport is 4 (Air)" in {
            val securityGen       = arbitrary[SecurityDetailsType](arbitrarySomeSecurityDetailsType)
            val identificationGen = Gen.oneOf(IataFlightNumber, RegNumberAircraft)
            forAll(securityGen, identificationGen) {
              (securityType, identification) =>
                val userAnswers = emptyUserAnswers
                  .setValue(SecurityDetailsTypePage, securityType)
                  .setValue(BorderModeOfTransportPage, BorderModeOfTransport.Air)
                  .setValue(IdentificationPage(index), identification)
                  .setValue(IdentificationNumberPage(index), identificationNumber)
                  .setValue(AddNationalityYesNoPage(index), false)
                  .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)

                val result: EitherType[TransportMeansActiveDomain] = UserAnswersReader[TransportMeansActiveDomain](
                  TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig)
                ).run(userAnswers)

                result.left.value.page mustBe ConveyanceReferenceNumberPage(index)
            }
          }

          "and border mode of transport is not 4 (Air)" in {
            val securityGen       = arbitrary[SecurityDetailsType](arbitrarySomeSecurityDetailsType)
            val borderModeGen     = Gen.oneOf(BorderModeOfTransport.values.filterNot(_ == Air))
            val identificationGen = Gen.oneOf(IataFlightNumber, RegNumberAircraft)
            forAll(securityGen, borderModeGen, identificationGen) {
              (securityType, borderMode, identification) =>
                val userAnswers = emptyUserAnswers
                  .setValue(SecurityDetailsTypePage, securityType)
                  .setValue(BorderModeOfTransportPage, borderMode)
                  .setValue(IdentificationPage(index), identification)
                  .setValue(IdentificationNumberPage(index), identificationNumber)
                  .setValue(AddNationalityYesNoPage(index), false)
                  .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)

                val result: EitherType[TransportMeansActiveDomain] = UserAnswersReader[TransportMeansActiveDomain](
                  TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig)
                ).run(userAnswers)

                result.left.value.page mustBe ConveyanceReferenceNumberYesNoPage(index)
            }
          }
        }

        "when security is 0 (No security)" in {
          val borderModeGen     = arbitrary[BorderModeOfTransport]
          val identificationGen = arbitrary[Identification]
          forAll(borderModeGen, identificationGen) {
            (borderMode, identification) =>
              val userAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, NoSecurityDetails)
                .setValue(BorderModeOfTransportPage, borderMode)
                .setValue(IdentificationPage(index), identification)
                .setValue(IdentificationNumberPage(index), identificationNumber)
                .setValue(AddNationalityYesNoPage(index), false)
                .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)

              val result: EitherType[TransportMeansActiveDomain] = UserAnswersReader[TransportMeansActiveDomain](
                TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig)
              ).run(userAnswers)

              result.left.value.page mustBe ConveyanceReferenceNumberYesNoPage(index)
          }
        }

        "when conveyance reference number needs to be answered" in {
          val borderModeGen     = arbitrary[BorderModeOfTransport]
          val identificationGen = arbitrary[Identification]
          forAll(borderModeGen, identificationGen) {
            (borderMode, identification) =>
              val userAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, NoSecurityDetails)
                .setValue(BorderModeOfTransportPage, borderMode)
                .setValue(IdentificationPage(index), identification)
                .setValue(IdentificationNumberPage(index), identificationNumber)
                .setValue(AddNationalityYesNoPage(index), false)
                .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)
                .setValue(ConveyanceReferenceNumberYesNoPage(index), true)

              val result: EitherType[TransportMeansActiveDomain] = UserAnswersReader[TransportMeansActiveDomain](
                TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig)
              ).run(userAnswers)

              result.left.value.page mustBe ConveyanceReferenceNumberPage(index)
          }
        }
      }
    }

    "during transition" - {
      "nationalityReader" - {
        "can not be parsed from user answers" - {
          "when border mode of transport is 2 (rail)" - {
            "and add registered country for vehicle yes/no is unanswered" in {
              val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, BorderModeOfTransport.ChannelTunnel)

              val result = TransitionTransportMeansActiveDomain.nationalityReader(index).run(userAnswers)

              result.left.value.page mustBe AddNationalityYesNoPage(index)
            }

            "and registered country for vehicle is unanswered" in {
              val userAnswers = emptyUserAnswers
                .setValue(BorderModeOfTransportPage, BorderModeOfTransport.ChannelTunnel)
                .setValue(AddNationalityYesNoPage(index), true)

              val result = TransitionTransportMeansActiveDomain.nationalityReader(index).run(userAnswers)

              result.left.value.page mustBe NationalityPage(index)
            }
          }

          "when border mode of transport is not 2 (rail)" - {
            "and registered country for vehicle is unanswered" in {
              forAll(arbitrary[Option[BorderModeOfTransport]](arbitraryOptionalNonRailBorderModeOfTransport)) {
                borderMode =>
                  val userAnswers = emptyUserAnswers
                    .setValue(BorderModeOfTransportPage, borderMode)

                  val result = TransitionTransportMeansActiveDomain.nationalityReader(index).run(userAnswers)

                  result.left.value.page mustBe NationalityPage(index)
              }
            }
          }
        }
      }

      "identificationReader" - {
        "can be parsed from user answers" - {
          "when identification is inferred" in {
            forAll(arbitrary[Identification]) {
              identification =>
                val userAnswers = emptyUserAnswers
                  .setValue(BorderModeOfTransportPage, BorderModeOfTransport.ChannelTunnel)
                  .setValue(InferredIdentificationPage(index), identification)

                val result = TransitionTransportMeansActiveDomain.identificationReader(index).run(userAnswers)

                result.value.value mustBe identification
            }
          }

          "when identification is not inferred" in {
            forAll(arbitrary[Identification]) {
              identification =>
                val userAnswers = emptyUserAnswers
                  .setValue(BorderModeOfTransportPage, BorderModeOfTransport.ChannelTunnel)
                  .setValue(IdentificationPage(index), identification)

                val result = TransitionTransportMeansActiveDomain.identificationReader(index).run(userAnswers)

                result.value.value mustBe identification
            }
          }
        }
        "can not be parsed from user answers" - {
          "when border mode of transport is 2 (rail)" - {
            "and identification type is unanswered" in {
              val userAnswers = emptyUserAnswers
                .setValue(BorderModeOfTransportPage, BorderModeOfTransport.ChannelTunnel)

              val result = TransitionTransportMeansActiveDomain.identificationReader(index).run(userAnswers)

              result.left.value.page mustBe IdentificationPage(index)
            }
          }

          "when registered country for vehicle is defined" - {
            "and identification type is unanswered" in {
              forAll(arbitrary[Nationality]) {
                nationality =>
                  val userAnswers = emptyUserAnswers
                    .setValue(NationalityPage(index), nationality)

                  val result = TransitionTransportMeansActiveDomain.identificationReader(index).run(userAnswers)

                  result.left.value.page mustBe IdentificationPage(index)
              }
            }
          }

          "when border mode of transport is not 2 (rail) and registered country for vehicle is undefined" - {
            "and add identification type yes/no is unanswered" in {
              forAll(arbitrary[Option[BorderModeOfTransport]](arbitraryOptionalNonRailBorderModeOfTransport)) {
                borderMode =>
                  val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, borderMode)

                  val result = TransitionTransportMeansActiveDomain.identificationReader(index).run(userAnswers)

                  result.left.value.page mustBe AddIdentificationYesNoPage(index)
              }
            }

            "and identification type is unanswered" in {
              forAll(arbitrary[Option[BorderModeOfTransport]](arbitraryOptionalNonRailBorderModeOfTransport)) {
                borderMode =>
                  val userAnswers = emptyUserAnswers
                    .setValue(BorderModeOfTransportPage, borderMode)
                    .setValue(AddIdentificationYesNoPage(index), true)

                  val result = TransitionTransportMeansActiveDomain.identificationReader(index).run(userAnswers)

                  result.left.value.page mustBe IdentificationPage(index)
              }
            }
          }
        }
      }

      "identificationNumberReader" - {
        "can not be parsed from user answers" - {
          "when border mode of transport is 2 (rail)" - {
            "and identification number is unanswered" in {
              val userAnswers = emptyUserAnswers
                .setValue(BorderModeOfTransportPage, BorderModeOfTransport.ChannelTunnel)

              val result = TransitionTransportMeansActiveDomain.identificationNumberReader(index).run(userAnswers)

              result.left.value.page mustBe IdentificationNumberPage(index)
            }
          }

          "when registered country for vehicle is defined" - {
            "and identification number is unanswered" in {
              forAll(arbitrary[Nationality]) {
                nationality =>
                  val userAnswers = emptyUserAnswers
                    .setValue(NationalityPage(index), nationality)

                  val result = TransitionTransportMeansActiveDomain.identificationNumberReader(index).run(userAnswers)

                  result.left.value.page mustBe IdentificationNumberPage(index)
              }
            }
          }

          "when border mode of transport is not 2 (rail) and registered country for vehicle is undefined" - {
            "and add identification number yes/no is unanswered" in {
              forAll(arbitrary[Option[BorderModeOfTransport]](arbitraryOptionalNonRailBorderModeOfTransport)) {
                borderMode =>
                  val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, borderMode)

                  val result = TransitionTransportMeansActiveDomain.identificationNumberReader(index).run(userAnswers)

                  result.left.value.page mustBe AddVehicleIdentificationNumberYesNoPage(index)
              }
            }

            "and identification number is unanswered" in {
              forAll(arbitrary[Option[BorderModeOfTransport]](arbitraryOptionalNonRailBorderModeOfTransport)) {
                borderMode =>
                  val userAnswers = emptyUserAnswers
                    .setValue(BorderModeOfTransportPage, borderMode)
                    .setValue(AddVehicleIdentificationNumberYesNoPage(index), true)

                  val result = TransitionTransportMeansActiveDomain.identificationNumberReader(index).run(userAnswers)

                  result.left.value.page mustBe IdentificationNumberPage(index)
              }
            }
          }
        }
      }

      "conveyanceReader" - {
        "can not be parsed from user answers" - {
          "when there is security and mode of transport is 4 (air)" - {
            "and conveyance reference number is undefined" in {
              forAll(arbitrary[SecurityDetailsType](arbitrarySomeSecurityDetailsType)) {
                securityType =>
                  val userAnswers = emptyUserAnswers
                    .setValue(SecurityDetailsTypePage, securityType)
                    .setValue(BorderModeOfTransportPage, BorderModeOfTransport.Air)

                  val result = TransitionTransportMeansActiveDomain.conveyanceReader(index).run(userAnswers)

                  result.left.value.page mustBe ConveyanceReferenceNumberPage(index)
              }
            }
          }

          "when there is no security" - {
            "and add conveyance reference number yes/no is undefined" in {
              forAll(arbitrary[Option[BorderModeOfTransport]]) {
                borderMode =>
                  val userAnswers = emptyUserAnswers
                    .setValue(SecurityDetailsTypePage, NoSecurityDetails)
                    .setValue(BorderModeOfTransportPage, borderMode)

                  val result = TransitionTransportMeansActiveDomain.conveyanceReader(index).run(userAnswers)

                  result.left.value.page mustBe ConveyanceReferenceNumberYesNoPage(index)
              }
            }
          }

          "when mode of transport is not 4 (air)" - {
            "and add conveyance reference number yes/no is undefined" in {
              forAll(arbitrary[SecurityDetailsType], arbitrary[Option[BorderModeOfTransport]](arbitraryOptionalNonAirBorderModeOfTransport)) {
                (securityType, borderMode) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(SecurityDetailsTypePage, securityType)
                    .setValue(BorderModeOfTransportPage, borderMode)

                  val result = TransitionTransportMeansActiveDomain.conveyanceReader(index).run(userAnswers)

                  result.left.value.page mustBe ConveyanceReferenceNumberYesNoPage(index)
              }
            }
          }
        }
      }
    }
  }
}
