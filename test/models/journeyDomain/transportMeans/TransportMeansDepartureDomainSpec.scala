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
import models.reference.transportMeans.departure.Identification
import models.reference.{InlandMode, Nationality}
import models.{OptionalBoolean, Phase}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.preRequisites.ContainerIndicatorPage
import pages.transportMeans.departure._
import pages.transportMeans.{AddDepartureTransportMeansYesNoPage, InlandModePage}

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
            .setValue(AddIdentificationTypeYesNoPage(departureIndex), true)
            .setValue(IdentificationPage(departureIndex), identification)
            .setValue(MeansIdentificationNumberPage(departureIndex), identificationNumber)
            .setValue(AddVehicleCountryYesNoPage(departureIndex), true)
            .setValue(VehicleCountryPage(departureIndex), nationality)

          val expectedResult = PostTransitionTransportMeansDepartureDomain(
            identification = Some(identification),
            identificationNumber = identificationNumber,
            nationality = Some(nationality)
          )(departureIndex)

          val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            AddIdentificationTypeYesNoPage(departureIndex),
            IdentificationPage(departureIndex),
            MeansIdentificationNumberPage(departureIndex),
            AddVehicleCountryYesNoPage(departureIndex),
            VehicleCountryPage
          )
        }
      }
    }

    "can not be parsed from user answers" - {

      "when post-transition" - {
        val mockPhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

        "when add identification page is missing" in {

          val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(emptyUserAnswers)

          result.left.value.page mustBe AddIdentificationTypeYesNoPage(departureIndex)
          result.left.value.pages mustBe Seq(
            AddIdentificationTypeYesNoPage(departureIndex)
          )
        }

        "when identification page is missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddIdentificationTypeYesNoPage(departureIndex), true)

          val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

          result.left.value.page mustBe IdentificationPage(departureIndex)
          result.left.value.pages mustBe Seq(
            AddIdentificationTypeYesNoPage(departureIndex),
            IdentificationPage(departureIndex)
          )
        }

        "when identification number page is missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddIdentificationTypeYesNoPage(departureIndex), true)
            .setValue(IdentificationPage(departureIndex), identification)

          val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

          result.left.value.page mustBe MeansIdentificationNumberPage(departureIndex)
          result.left.value.pages mustBe Seq(
            AddIdentificationTypeYesNoPage(departureIndex),
            IdentificationPage(departureIndex),
            MeansIdentificationNumberPage(departureIndex)
          )
        }

        "when add vehicle country page is missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddIdentificationTypeYesNoPage(departureIndex), true)
            .setValue(IdentificationPage(departureIndex), identification)
            .setValue(MeansIdentificationNumberPage(departureIndex), identificationNumber)

          val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

          result.left.value.page mustBe AddVehicleCountryYesNoPage(departureIndex)
          result.left.value.pages mustBe Seq(
            AddIdentificationTypeYesNoPage(departureIndex),
            IdentificationPage(departureIndex),
            MeansIdentificationNumberPage(departureIndex),
            AddVehicleCountryYesNoPage(departureIndex)
          )
        }

        "when vehicle country page is missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddIdentificationTypeYesNoPage(departureIndex), true)
            .setValue(IdentificationPage(departureIndex), identification)
            .setValue(MeansIdentificationNumberPage(departureIndex), identificationNumber)
            .setValue(AddVehicleCountryYesNoPage(departureIndex), true)

          val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

          result.left.value.page mustBe VehicleCountryPage(departureIndex)
          result.left.value.pages mustBe Seq(
            AddIdentificationTypeYesNoPage(departureIndex),
            IdentificationPage(departureIndex),
            MeansIdentificationNumberPage(departureIndex),
            AddVehicleCountryYesNoPage(departureIndex),
            VehicleCountryPage(departureIndex)
          )
        }
      }

      "when during transition" - {
        val mockPhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

        "and container indicator is 1" - {
          "and add identification type yes/no page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
              .setValue(AddDepartureTransportMeansYesNoPage, true)

            val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe AddIdentificationTypeYesNoPage(departureIndex)
            result.left.value.pages mustBe Seq(
              AddIdentificationTypeYesNoPage(departureIndex)
            )
          }

          "and identification page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
              .setValue(AddDepartureTransportMeansYesNoPage, true)
              .setValue(AddIdentificationTypeYesNoPage(departureIndex), true)

            val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe IdentificationPage(departureIndex)
            result.left.value.pages mustBe Seq(
              AddIdentificationTypeYesNoPage(departureIndex),
              IdentificationPage(departureIndex)
            )
          }

          "and add identification number yes/no page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
              .setValue(AddDepartureTransportMeansYesNoPage, true)
              .setValue(AddIdentificationTypeYesNoPage(departureIndex), false)

            val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe AddIdentificationNumberYesNoPage(departureIndex)
            result.left.value.pages mustBe Seq(
              AddIdentificationTypeYesNoPage(departureIndex),
              AddIdentificationNumberYesNoPage(departureIndex)
            )
          }

          "and identification number page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
              .setValue(AddDepartureTransportMeansYesNoPage, true)
              .setValue(AddIdentificationTypeYesNoPage(departureIndex), false)
              .setValue(AddIdentificationNumberYesNoPage(departureIndex), true)

            val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe MeansIdentificationNumberPage(departureIndex)
            result.left.value.pages mustBe Seq(
              AddIdentificationTypeYesNoPage(departureIndex),
              AddIdentificationNumberYesNoPage(departureIndex),
              MeansIdentificationNumberPage(departureIndex)
            )
          }

          "and add nationality yes/no page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
              .setValue(AddDepartureTransportMeansYesNoPage, true)
              .setValue(InlandModePage, arbitrary[InlandMode](arbitraryNonRailInlandMode).sample.value)
              .setValue(AddIdentificationTypeYesNoPage(departureIndex), false)
              .setValue(AddIdentificationNumberYesNoPage(departureIndex), false)

            val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe AddVehicleCountryYesNoPage(departureIndex)
            result.left.value.pages mustBe Seq(
              AddIdentificationTypeYesNoPage(departureIndex),
              AddIdentificationNumberYesNoPage(departureIndex),
              AddVehicleCountryYesNoPage(departureIndex)
            )
          }

          "and nationality page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
              .setValue(AddDepartureTransportMeansYesNoPage, true)
              .setValue(InlandModePage, arbitrary[InlandMode](arbitraryNonRailInlandMode).sample.value)
              .setValue(AddIdentificationTypeYesNoPage(departureIndex), false)
              .setValue(AddIdentificationNumberYesNoPage(departureIndex), false)
              .setValue(AddVehicleCountryYesNoPage(departureIndex), true)

            val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe VehicleCountryPage(departureIndex)
            result.left.value.pages mustBe Seq(
              AddIdentificationTypeYesNoPage(departureIndex),
              AddIdentificationNumberYesNoPage(departureIndex),
              AddVehicleCountryYesNoPage(departureIndex),
              VehicleCountryPage(departureIndex)
            )
          }
        }

        "and container indicator is 0" - {
          val containerIndicator = OptionalBoolean.no
          "and identification type page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, containerIndicator)
              .setValue(AddDepartureTransportMeansYesNoPage, true)

            val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe IdentificationPage(departureIndex)
            result.left.value.pages mustBe Seq(
              IdentificationPage(departureIndex)
            )
          }

          "and identification number page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, containerIndicator)
              .setValue(IdentificationPage(departureIndex), arbitrary[Identification].sample.value)

            val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe MeansIdentificationNumberPage(departureIndex)
            result.left.value.pages mustBe Seq(
              IdentificationPage(departureIndex),
              MeansIdentificationNumberPage(departureIndex)
            )
          }
        }

        "and container indicator is 'not sure'" - {
          val containerIndicator = OptionalBoolean.maybe
          "and identification type page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, containerIndicator)
              .setValue(AddDepartureTransportMeansYesNoPage, true)
              .setValue(AddIdentificationTypeYesNoPage(departureIndex), true)

            val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe IdentificationPage(departureIndex)
            result.left.value.pages mustBe Seq(
              AddIdentificationTypeYesNoPage(departureIndex),
              IdentificationPage(departureIndex)
            )
          }

          "and identification number page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, containerIndicator)
              .setValue(IdentificationPage(departureIndex), arbitrary[Identification].sample.value)
              .setValue(AddIdentificationNumberYesNoPage(departureIndex), true)

            val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe MeansIdentificationNumberPage
            result.left.value.pages mustBe Seq(
              IdentificationPage(departureIndex),
              AddIdentificationNumberYesNoPage(departureIndex),
              MeansIdentificationNumberPage(departureIndex)
            )
          }
        }

        "and container indicator is 0" - {
          "and nationality page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.no)
              .setValue(InlandModePage, arbitrary[InlandMode](arbitraryNonRailInlandMode).sample.value)
              .setValue(IdentificationPage(departureIndex), arbitrary[Identification].sample.value)
              .setValue(MeansIdentificationNumberPage(departureIndex), Gen.alphaNumStr.sample.value)

            val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe VehicleCountryPage(departureIndex)
            result.left.value.pages mustBe Seq(
              IdentificationPage(departureIndex),
              MeansIdentificationNumberPage(departureIndex),
              VehicleCountryPage(departureIndex)
            )
          }
        }

        "and container indicator is not sure" - {
          "and nationality page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.maybe)
              .setValue(InlandModePage, arbitrary[InlandMode](arbitraryNonRailInlandMode).sample.value)
              .setValue(IdentificationPage(departureIndex), arbitrary[Identification].sample.value)
              .setValue(AddIdentificationNumberYesNoPage(departureIndex), true)
              .setValue(MeansIdentificationNumberPage(departureIndex), Gen.alphaNumStr.sample.value)

            val result = TransportMeansDepartureDomain.userAnswersReader(departureIndex)(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe VehicleCountryPage(departureIndex)
            result.left.value.pages mustBe Seq(
              IdentificationPage(departureIndex),
              AddIdentificationNumberYesNoPage(departureIndex),
              MeansIdentificationNumberPage(departureIndex),
              VehicleCountryPage(departureIndex)
            )
          }
        }
      }
    }
  }
}
