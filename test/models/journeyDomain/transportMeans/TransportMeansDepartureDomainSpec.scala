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
            .setValue(AddIdentificationTypeYesNoPage, true)
            .setValue(IdentificationPage, identification)
            .setValue(MeansIdentificationNumberPage, identificationNumber)
            .setValue(AddVehicleCountryYesNoPage, true)
            .setValue(VehicleCountryPage, nationality)

          val expectedResult = PostTransitionTransportMeansDepartureDomain(
            identification = Some(identification),
            identificationNumber = identificationNumber,
            nationality = Some(nationality)
          )

          val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            AddIdentificationTypeYesNoPage,
            IdentificationPage,
            MeansIdentificationNumberPage,
            AddVehicleCountryYesNoPage,
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

          val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(emptyUserAnswers)

          result.left.value.page mustBe AddIdentificationTypeYesNoPage
          result.left.value.pages mustBe Seq(
            AddIdentificationTypeYesNoPage
          )
        }

        "when identification page is missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddIdentificationTypeYesNoPage, true)

          val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

          result.left.value.page mustBe IdentificationPage
          result.left.value.pages mustBe Seq(
            AddIdentificationTypeYesNoPage,
            IdentificationPage
          )
        }

        "when identification number page is missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddIdentificationTypeYesNoPage, true)
            .setValue(IdentificationPage, identification)

          val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

          result.left.value.page mustBe MeansIdentificationNumberPage
          result.left.value.pages mustBe Seq(
            AddIdentificationTypeYesNoPage,
            IdentificationPage,
            MeansIdentificationNumberPage
          )
        }

        "when add vehicle country page is missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddIdentificationTypeYesNoPage, true)
            .setValue(IdentificationPage, identification)
            .setValue(MeansIdentificationNumberPage, identificationNumber)

          val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

          result.left.value.page mustBe AddVehicleCountryYesNoPage
          result.left.value.pages mustBe Seq(
            AddIdentificationTypeYesNoPage,
            IdentificationPage,
            MeansIdentificationNumberPage,
            AddVehicleCountryYesNoPage
          )
        }

        "when vehicle country page is missing" in {
          val userAnswers = emptyUserAnswers
            .setValue(AddIdentificationTypeYesNoPage, true)
            .setValue(IdentificationPage, identification)
            .setValue(MeansIdentificationNumberPage, identificationNumber)
            .setValue(AddVehicleCountryYesNoPage, true)

          val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

          result.left.value.page mustBe VehicleCountryPage
          result.left.value.pages mustBe Seq(
            AddIdentificationTypeYesNoPage,
            IdentificationPage,
            MeansIdentificationNumberPage,
            AddVehicleCountryYesNoPage,
            VehicleCountryPage
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

            val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe AddIdentificationTypeYesNoPage
            result.left.value.pages mustBe Seq(
              AddIdentificationTypeYesNoPage
            )
          }

          "and identification page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
              .setValue(AddDepartureTransportMeansYesNoPage, true)
              .setValue(AddIdentificationTypeYesNoPage, true)

            val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe IdentificationPage
            result.left.value.pages mustBe Seq(
              AddIdentificationTypeYesNoPage,
              IdentificationPage
            )
          }

          "and add identification number yes/no page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
              .setValue(AddDepartureTransportMeansYesNoPage, true)
              .setValue(AddIdentificationTypeYesNoPage, false)

            val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe AddIdentificationNumberYesNoPage
            result.left.value.pages mustBe Seq(
              AddIdentificationTypeYesNoPage,
              AddIdentificationNumberYesNoPage
            )
          }

          "and identification number page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
              .setValue(AddDepartureTransportMeansYesNoPage, true)
              .setValue(AddIdentificationTypeYesNoPage, false)
              .setValue(AddIdentificationNumberYesNoPage, true)

            val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe MeansIdentificationNumberPage
            result.left.value.pages mustBe Seq(
              AddIdentificationTypeYesNoPage,
              AddIdentificationNumberYesNoPage,
              MeansIdentificationNumberPage
            )
          }

          "and add nationality yes/no page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
              .setValue(AddDepartureTransportMeansYesNoPage, true)
              .setValue(InlandModePage, arbitrary[InlandMode](arbitraryNonRailInlandMode).sample.value)
              .setValue(AddIdentificationTypeYesNoPage, false)
              .setValue(AddIdentificationNumberYesNoPage, false)

            val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe AddVehicleCountryYesNoPage
            result.left.value.pages mustBe Seq(
              AddIdentificationTypeYesNoPage,
              AddIdentificationNumberYesNoPage,
              AddVehicleCountryYesNoPage
            )
          }

          "and nationality page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
              .setValue(AddDepartureTransportMeansYesNoPage, true)
              .setValue(InlandModePage, arbitrary[InlandMode](arbitraryNonRailInlandMode).sample.value)
              .setValue(AddIdentificationTypeYesNoPage, false)
              .setValue(AddIdentificationNumberYesNoPage, false)
              .setValue(AddVehicleCountryYesNoPage, true)

            val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe VehicleCountryPage
            result.left.value.pages mustBe Seq(
              AddIdentificationTypeYesNoPage,
              AddIdentificationNumberYesNoPage,
              AddVehicleCountryYesNoPage,
              VehicleCountryPage
            )
          }
        }

        "and container indicator is 0" - {
          val containerIndicator = OptionalBoolean.no
          "and identification type page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, containerIndicator)
              .setValue(AddDepartureTransportMeansYesNoPage, true)

            val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe IdentificationPage
            result.left.value.pages mustBe Seq(
              IdentificationPage
            )
          }

          "and identification number page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, containerIndicator)
              .setValue(IdentificationPage, arbitrary[Identification].sample.value)

            val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe MeansIdentificationNumberPage
            result.left.value.pages mustBe Seq(
              IdentificationPage,
              MeansIdentificationNumberPage
            )
          }
        }

        "and container indicator is 'not sure'" - {
          val containerIndicator = OptionalBoolean.maybe
          "and identification type page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, containerIndicator)
              .setValue(AddDepartureTransportMeansYesNoPage, true)
              .setValue(AddIdentificationTypeYesNoPage, true)

            val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe IdentificationPage
            result.left.value.pages mustBe Seq(
              AddIdentificationTypeYesNoPage,
              IdentificationPage
            )
          }

          "and identification number page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, containerIndicator)
              .setValue(IdentificationPage, arbitrary[Identification].sample.value)
              .setValue(AddIdentificationNumberYesNoPage, true)

            val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe MeansIdentificationNumberPage
            result.left.value.pages mustBe Seq(
              IdentificationPage,
              AddIdentificationNumberYesNoPage,
              MeansIdentificationNumberPage
            )
          }
        }

        "and container indicator is 0" - {
          "and nationality page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.no)
              .setValue(InlandModePage, arbitrary[InlandMode](arbitraryNonRailInlandMode).sample.value)
              .setValue(IdentificationPage, arbitrary[Identification].sample.value)
              .setValue(MeansIdentificationNumberPage, Gen.alphaNumStr.sample.value)

            val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe VehicleCountryPage
            result.left.value.pages mustBe Seq(
              IdentificationPage,
              MeansIdentificationNumberPage,
              VehicleCountryPage
            )
          }
        }

        "and container indicator is not sure" - {
          "and nationality page is missing" in {
            val userAnswers = emptyUserAnswers
              .setValue(ContainerIndicatorPage, OptionalBoolean.maybe)
              .setValue(InlandModePage, arbitrary[InlandMode](arbitraryNonRailInlandMode).sample.value)
              .setValue(IdentificationPage, arbitrary[Identification].sample.value)
              .setValue(AddIdentificationNumberYesNoPage, true)
              .setValue(MeansIdentificationNumberPage, Gen.alphaNumStr.sample.value)

            val result = TransportMeansDepartureDomain.userAnswersReader(mockPhaseConfig).apply(Nil).run(userAnswers)

            result.left.value.page mustBe VehicleCountryPage
            result.left.value.pages mustBe Seq(
              IdentificationPage,
              AddIdentificationNumberYesNoPage,
              MeansIdentificationNumberPage,
              VehicleCountryPage
            )
          }
        }
      }
    }
  }
}
