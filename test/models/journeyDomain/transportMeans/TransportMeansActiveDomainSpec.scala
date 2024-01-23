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
import config.Constants.SecurityType._
import config.PhaseConfig
import generators.Generators
import models.Phase
import models.reference.transportMeans.active.Identification
import models.reference.{BorderMode, CustomsOffice, Nationality}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.SecurityDetailsTypePage
import pages.sections.external.OfficesOfTransitSection
import pages.sections.transportMeans.{ActiveSection, TransportMeansSection}
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.active._
import play.api.libs.json.{JsArray, Json}

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

      "hasMultiplicity" - {
        "when at least one office of transit added" - {
          "must be true" in {
            val userAnswers = emptyUserAnswers.setValue(OfficesOfTransitSection, JsArray(Seq(Json.obj("foo" -> "bar"))))
            val result      = TransportMeansActiveDomain.hasMultiplicity(userAnswers, Phase.PostTransition)
            result mustBe true
          }
        }

        "when no offices of transit added" - {
          "must be false" in {
            val result = TransportMeansActiveDomain.hasMultiplicity(emptyUserAnswers, Phase.PostTransition)
            result mustBe false
          }
        }
      }

      "can be parsed from user answers" - {
        "when the add nationality is answered yes" - {
          "and security detail type is 0 and inland mode is Sea and add conveyance number is yes" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(OfficesOfTransitSection, JsArray(Seq(Json.obj("foo" -> "bar"))))
              .setValue(BorderModeOfTransportPage, BorderMode("1", "Maritime"))
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

            val result = TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

            result.value.value mustBe expectedResult
            result.value.pages mustBe Seq(
              IdentificationPage(index),
              IdentificationNumberPage(index),
              AddNationalityYesNoPage(index),
              NationalityPage(index),
              CustomsOfficeActiveBorderPage(index),
              ConveyanceReferenceNumberYesNoPage(index),
              ConveyanceReferenceNumberPage(index),
              ActiveSection(index)
            )
          }

          "and security detail type is 1 and inland mode is Air" in {
            val securityGen = arbitrary[String](arbitrarySomeSecurityDetailsType)
            forAll(securityGen) {
              securityType =>
                val userAnswers = emptyUserAnswers
                  .setValue(SecurityDetailsTypePage, securityType)
                  .setValue(BorderModeOfTransportPage, BorderMode("4", "Air"))
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

                val result = TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

                result.value.value mustBe expectedResult
                result.value.pages mustBe Seq(
                  IdentificationPage(index),
                  IdentificationNumberPage(index),
                  AddNationalityYesNoPage(index),
                  NationalityPage(index),
                  CustomsOfficeActiveBorderPage(index),
                  ConveyanceReferenceNumberPage(index),
                  TransportMeansSection
                )
            }
          }
        }

        "when the add nationality is answered no" - {
          "and security detail type is 0 and inland mode is Sea and add conveyance number is no" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(BorderModeOfTransportPage, BorderMode("1", "Sea"))
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

            val result = TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

            result.value.value mustBe expectedResult
            result.value.pages mustBe Seq(
              IdentificationPage(index),
              IdentificationNumberPage(index),
              AddNationalityYesNoPage(index),
              CustomsOfficeActiveBorderPage(index),
              ConveyanceReferenceNumberYesNoPage(index),
              TransportMeansSection
            )
          }
        }
      }

      "can not be parsed from user answers" - {
        "when border mode is answered" - {
          "must go to identification type" in {
            forAll(arbitrary[BorderMode]) {
              borderModeOfTransport =>
                val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, borderModeOfTransport)

                val result = TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

                result.left.value.page mustBe IdentificationPage(index)
                result.left.value.pages mustBe Seq(
                  IdentificationPage(index)
                )
            }
          }
        }

        "when add nationality is unanswered" in {
          val userAnswers = emptyUserAnswers
            .setValue(BorderModeOfTransportPage, BorderMode("3", "Road"))
            .setValue(InferredIdentificationPage(index), Identification("30", "Registration number of a road vehicle"))
            .setValue(IdentificationNumberPage(index), identificationNumber)

          val result = TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

          result.left.value.page mustBe AddNationalityYesNoPage(index)
          result.left.value.pages mustBe Seq(
            IdentificationNumberPage(index),
            AddNationalityYesNoPage(index)
          )
        }

        "when nationality is unanswered" in {
          val userAnswers = emptyUserAnswers
            .setValue(BorderModeOfTransportPage, BorderMode("4", "Air"))
            .setValue(IdentificationPage(index), Identification("40", "IATA flight number"))
            .setValue(IdentificationNumberPage(index), identificationNumber)
            .setValue(AddNationalityYesNoPage(index), true)

          val result = TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

          result.left.value.page mustBe NationalityPage(index)
          result.left.value.pages mustBe Seq(
            IdentificationPage(index),
            IdentificationNumberPage(index),
            AddNationalityYesNoPage(index),
            NationalityPage(index)
          )
        }

        "when customs office ref. number is unanswered" - {
          "and add nationality is true" in {
            val userAnswers = emptyUserAnswers
              .setValue(BorderModeOfTransportPage, BorderMode("3", "Road"))
              .setValue(InferredIdentificationPage(index), Identification("30", "Registration number of a road vehicle"))
              .setValue(IdentificationNumberPage(index), identificationNumber)
              .setValue(AddNationalityYesNoPage(index), true)
              .setValue(NationalityPage(index), nationality)

            val result = TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe CustomsOfficeActiveBorderPage(index)
            result.left.value.pages mustBe Seq(
              IdentificationNumberPage(index),
              AddNationalityYesNoPage(index),
              NationalityPage(index),
              CustomsOfficeActiveBorderPage(index)
            )
          }

          "and add nationality is false" in {
            val userAnswers = emptyUserAnswers
              .setValue(BorderModeOfTransportPage, BorderMode("3", "Road"))
              .setValue(InferredIdentificationPage(index), Identification("30", "Registration number of a road vehicle"))
              .setValue(IdentificationNumberPage(index), identificationNumber)
              .setValue(AddNationalityYesNoPage(index), false)

            val result = TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe CustomsOfficeActiveBorderPage(index)
            result.left.value.pages mustBe Seq(
              IdentificationNumberPage(index),
              AddNationalityYesNoPage(index),
              CustomsOfficeActiveBorderPage(index)
            )
          }
        }

        "when security is in set {1,2,3}" - {
          "and border mode of transport is 4 (Air)" in {
            val securityGen       = arbitrary[String](arbitrarySomeSecurityDetailsType)
            val identificationGen = Gen.oneOf(Identification("40", "IATA flight number"), Identification("41", "Registration number of an aircraft"))
            forAll(securityGen, identificationGen) {
              (securityType, identification) =>
                val userAnswers = emptyUserAnswers
                  .setValue(SecurityDetailsTypePage, securityType)
                  .setValue(BorderModeOfTransportPage, BorderMode("4", "Air"))
                  .setValue(IdentificationPage(index), identification)
                  .setValue(IdentificationNumberPage(index), identificationNumber)
                  .setValue(AddNationalityYesNoPage(index), false)
                  .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)

                val result = TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

                result.left.value.page mustBe ConveyanceReferenceNumberPage(index)
                result.left.value.pages mustBe Seq(
                  IdentificationPage(index),
                  IdentificationNumberPage(index),
                  AddNationalityYesNoPage(index),
                  CustomsOfficeActiveBorderPage(index),
                  ConveyanceReferenceNumberPage(index)
                )
            }
          }

          "and border mode of transport is not 4 (Air)" in {
            val securityGen       = arbitrary[String](arbitrarySomeSecurityDetailsType)
            val borderModeGen     = arbitrary[Option[BorderMode]](arbitraryOptionalNonAirBorderModeOfTransport).sample.value
            val identificationGen = Gen.oneOf(Identification("40", "IATA flight number"), Identification("41", "Registration number of an aircraft"))
            forAll(securityGen, borderModeGen, identificationGen) {
              (securityType, borderMode, identification) =>
                val userAnswers = emptyUserAnswers
                  .setValue(SecurityDetailsTypePage, securityType)
                  .setValue(BorderModeOfTransportPage, borderMode)
                  .setValue(IdentificationPage(index), identification)
                  .setValue(IdentificationNumberPage(index), identificationNumber)
                  .setValue(AddNationalityYesNoPage(index), false)
                  .setValue(CustomsOfficeActiveBorderPage(index), customsOffice)

                val result = TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

                result.left.value.page mustBe ConveyanceReferenceNumberYesNoPage(index)
                result.left.value.pages mustBe Seq(
                  IdentificationPage(index),
                  IdentificationNumberPage(index),
                  AddNationalityYesNoPage(index),
                  CustomsOfficeActiveBorderPage(index),
                  ConveyanceReferenceNumberYesNoPage(index)
                )
            }
          }
        }

        "when security is 0 (No security)" in {
          val borderModeGen     = arbitrary[BorderMode]
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

              val result = TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

              result.left.value.page mustBe ConveyanceReferenceNumberYesNoPage(index)
              result.left.value.pages mustBe Seq(
                IdentificationPage(index),
                IdentificationNumberPage(index),
                AddNationalityYesNoPage(index),
                CustomsOfficeActiveBorderPage(index),
                ConveyanceReferenceNumberYesNoPage(index)
              )
          }
        }

        "when conveyance reference number needs to be answered" in {
          val borderModeGen     = arbitrary[BorderMode]
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

              val result = TransportMeansActiveDomain.userAnswersReader(index)(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

              result.left.value.page mustBe ConveyanceReferenceNumberPage(index)
              result.left.value.pages mustBe Seq(
                IdentificationPage(index),
                IdentificationNumberPage(index),
                AddNationalityYesNoPage(index),
                CustomsOfficeActiveBorderPage(index),
                ConveyanceReferenceNumberYesNoPage(index),
                ConveyanceReferenceNumberPage(index)
              )
          }
        }
      }
    }

    "during transition" - {

      "hasMultiplicity" - {
        "when at least one office of transit added" - {
          "must be false" in {
            val userAnswers = emptyUserAnswers.setValue(OfficesOfTransitSection, JsArray(Seq(Json.obj("foo" -> "bar"))))
            val result      = TransportMeansActiveDomain.hasMultiplicity(userAnswers, Phase.Transition)
            result mustBe false
          }
        }

        "when no offices of transit added" - {
          "must be false" in {
            val result = TransportMeansActiveDomain.hasMultiplicity(emptyUserAnswers, Phase.Transition)
            result mustBe false
          }
        }
      }

      "nationalityReader" - {
        "can not be parsed from user answers" - {
          "when border mode of transport is 2 (rail)" - {
            "and add registered country for vehicle yes/no is unanswered" in {
              val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, BorderMode("2", "Rail"))

              val result = TransitionTransportMeansActiveDomain.nationalityReader(index).apply(Nil).run(userAnswers)

              result.left.value.page mustBe AddNationalityYesNoPage(index)
              result.left.value.pages mustBe Seq(
                AddNationalityYesNoPage(index)
              )
            }

            "and registered country for vehicle is unanswered" in {
              val userAnswers = emptyUserAnswers
                .setValue(BorderModeOfTransportPage, BorderMode("2", "Rail"))
                .setValue(AddNationalityYesNoPage(index), true)

              val result = TransitionTransportMeansActiveDomain.nationalityReader(index).apply(Nil).run(userAnswers)

              result.left.value.page mustBe NationalityPage(index)
              result.left.value.pages mustBe Seq(
                AddNationalityYesNoPage(index),
                NationalityPage(index)
              )
            }
          }

          "when border mode of transport is not 2 (rail)" - {
            "and registered country for vehicle is unanswered" in {
              forAll(arbitrary[Option[BorderMode]](arbitraryOptionalNonRailBorderModeOfTransport)) {
                borderMode =>
                  val userAnswers = emptyUserAnswers
                    .setValue(BorderModeOfTransportPage, borderMode)

                  val result = TransitionTransportMeansActiveDomain.nationalityReader(index).apply(Nil).run(userAnswers)

                  result.left.value.page mustBe NationalityPage(index)
                  result.left.value.pages mustBe Seq(
                    NationalityPage(index)
                  )
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
                  .setValue(BorderModeOfTransportPage, BorderMode("2", "Rail"))
                  .setValue(InferredIdentificationPage(index), identification)

                val result = TransitionTransportMeansActiveDomain.identificationReader(index).apply(Nil).run(userAnswers)

                result.value.value.value mustBe identification
                result.value.pages mustBe Nil
            }
          }

          "when identification is not inferred" in {
            forAll(arbitrary[Identification]) {
              identification =>
                val userAnswers = emptyUserAnswers
                  .setValue(BorderModeOfTransportPage, BorderMode("2", "Rail"))
                  .setValue(IdentificationPage(index), identification)

                val result = TransitionTransportMeansActiveDomain.identificationReader(index).apply(Nil).run(userAnswers)

                result.value.value.value mustBe identification
                result.value.pages mustBe Seq(
                  IdentificationPage(index)
                )
            }
          }
        }
        "can not be parsed from user answers" - {
          "when border mode of transport is 2 (rail)" - {
            "and identification type is unanswered" in {
              val userAnswers = emptyUserAnswers
                .setValue(BorderModeOfTransportPage, BorderMode("2", "Rail"))

              val result = TransitionTransportMeansActiveDomain.identificationReader(index).apply(Nil).run(userAnswers)

              result.left.value.page mustBe IdentificationPage(index)
              result.left.value.pages mustBe Seq(
                IdentificationPage(index)
              )
            }
          }

          "when registered country for vehicle is defined" - {
            "and identification type is unanswered" in {
              forAll(arbitrary[Nationality]) {
                nationality =>
                  val userAnswers = emptyUserAnswers
                    .setValue(NationalityPage(index), nationality)

                  val result = TransitionTransportMeansActiveDomain.identificationReader(index).apply(Nil).run(userAnswers)

                  result.left.value.page mustBe IdentificationPage(index)
                  result.left.value.pages mustBe Seq(
                    IdentificationPage(index)
                  )
              }
            }
          }

          "when border mode of transport is not 2 (rail) and registered country for vehicle is undefined" - {
            "and add identification type yes/no is unanswered" in {
              forAll(arbitrary[Option[BorderMode]](arbitraryOptionalNonRailBorderModeOfTransport)) {
                borderMode =>
                  val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, borderMode)

                  val result = TransitionTransportMeansActiveDomain.identificationReader(index).apply(Nil).run(userAnswers)

                  result.left.value.page mustBe AddIdentificationYesNoPage(index)
                  result.left.value.pages mustBe Seq(
                    AddIdentificationYesNoPage(index)
                  )
              }
            }

            "and identification type is unanswered" in {
              forAll(arbitrary[Option[BorderMode]](arbitraryOptionalNonRailBorderModeOfTransport)) {
                borderMode =>
                  val userAnswers = emptyUserAnswers
                    .setValue(BorderModeOfTransportPage, borderMode)
                    .setValue(AddIdentificationYesNoPage(index), true)

                  val result = TransitionTransportMeansActiveDomain.identificationReader(index).apply(Nil).run(userAnswers)

                  result.left.value.page mustBe IdentificationPage(index)
                  result.left.value.pages mustBe Seq(
                    AddIdentificationYesNoPage(index),
                    IdentificationPage(index)
                  )
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
                .setValue(BorderModeOfTransportPage, BorderMode("2", "Rail"))

              val result = TransitionTransportMeansActiveDomain.identificationNumberReader(index).apply(Nil).run(userAnswers)

              result.left.value.page mustBe IdentificationNumberPage(index)
              result.left.value.pages mustBe Seq(
                IdentificationNumberPage(index)
              )
            }
          }

          "when registered country for vehicle is defined" - {
            "and identification number is unanswered" in {
              forAll(arbitrary[Nationality]) {
                nationality =>
                  val userAnswers = emptyUserAnswers
                    .setValue(NationalityPage(index), nationality)

                  val result = TransitionTransportMeansActiveDomain.identificationNumberReader(index).apply(Nil).run(userAnswers)

                  result.left.value.page mustBe IdentificationNumberPage(index)
                  result.left.value.pages mustBe Seq(
                    IdentificationNumberPage(index)
                  )
              }
            }
          }

          "when border mode of transport is not 2 (rail) and registered country for vehicle is undefined" - {
            "and add identification number yes/no is unanswered" in {
              forAll(arbitrary[Option[BorderMode]](arbitraryOptionalNonRailBorderModeOfTransport)) {
                borderMode =>
                  val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, borderMode)

                  val result = TransitionTransportMeansActiveDomain.identificationNumberReader(index).apply(Nil).run(userAnswers)

                  result.left.value.page mustBe AddVehicleIdentificationNumberYesNoPage(index)
                  result.left.value.pages mustBe Seq(
                    AddVehicleIdentificationNumberYesNoPage(index)
                  )
              }
            }

            "and identification number is unanswered" in {
              forAll(arbitrary[Option[BorderMode]](arbitraryOptionalNonRailBorderModeOfTransport)) {
                borderMode =>
                  val userAnswers = emptyUserAnswers
                    .setValue(BorderModeOfTransportPage, borderMode)
                    .setValue(AddVehicleIdentificationNumberYesNoPage(index), true)

                  val result = TransitionTransportMeansActiveDomain.identificationNumberReader(index).apply(Nil).run(userAnswers)

                  result.left.value.page mustBe IdentificationNumberPage(index)
                  result.left.value.pages mustBe Seq(
                    AddVehicleIdentificationNumberYesNoPage(index),
                    IdentificationNumberPage(index)
                  )
              }
            }
          }
        }
      }

      "conveyanceReader" - {
        "can not be parsed from user answers" - {
          "when there is security and mode of transport is 4 (air)" - {
            "and conveyance reference number is undefined" in {
              forAll(arbitrary[String](arbitrarySomeSecurityDetailsType)) {
                securityType =>
                  val userAnswers = emptyUserAnswers
                    .setValue(SecurityDetailsTypePage, securityType)
                    .setValue(BorderModeOfTransportPage, BorderMode("4", "Air"))

                  val result = TransportMeansActiveDomain.conveyanceReader(index).apply(Nil).run(userAnswers)

                  result.left.value.page mustBe ConveyanceReferenceNumberPage(index)
                  result.left.value.pages mustBe Seq(
                    ConveyanceReferenceNumberPage(index)
                  )
              }
            }
          }

          "when there is no security" - {
            "and add conveyance reference number yes/no is undefined" in {
              forAll(arbitrary[Option[BorderMode]]) {
                borderMode =>
                  val userAnswers = emptyUserAnswers
                    .setValue(SecurityDetailsTypePage, NoSecurityDetails)
                    .setValue(BorderModeOfTransportPage, borderMode)

                  val result = TransportMeansActiveDomain.conveyanceReader(index).apply(Nil).run(userAnswers)

                  result.left.value.page mustBe ConveyanceReferenceNumberYesNoPage(index)
                  result.left.value.pages mustBe Seq(
                    ConveyanceReferenceNumberYesNoPage(index)
                  )
              }
            }
          }

          "when mode of transport is not 4 (air)" - {
            "and add conveyance reference number yes/no is undefined" in {
              forAll(arbitrary[String](arbitrarySecurityDetailsType), arbitrary[Option[BorderMode]](arbitraryOptionalNonAirBorderModeOfTransport)) {
                (securityType, borderMode) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(SecurityDetailsTypePage, securityType)
                    .setValue(BorderModeOfTransportPage, borderMode)

                  val result = TransportMeansActiveDomain.conveyanceReader(index).apply(Nil).run(userAnswers)

                  result.left.value.page mustBe ConveyanceReferenceNumberYesNoPage(index)
                  result.left.value.pages mustBe Seq(
                    ConveyanceReferenceNumberYesNoPage(index)
                  )
              }
            }
          }
        }
      }
    }
  }
}
