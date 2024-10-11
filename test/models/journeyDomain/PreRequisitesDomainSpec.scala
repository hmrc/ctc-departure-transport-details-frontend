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

package models.journeyDomain

import base.SpecBase
import config.Constants.DeclarationType.*
import config.PhaseConfig
import generators.Generators
import models.reference.Country
import models.{OptionalBoolean, Phase}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.QuestionPage
import pages.external.DeclarationTypePage
import pages.preRequisites.*

class PreRequisitesDomainSpec extends SpecBase with Generators {

  private val ucr                                    = Gen.alphaNumStr.sample.value
  private val country                                = arbitrary[Country].sample.value
  private val itemsDestinationCountry                = arbitrary[Country].sample.value
  private val mockTransitionPhaseConfig: PhaseConfig = mock[PhaseConfig]
  when(mockTransitionPhaseConfig.phase).thenReturn(Phase.Transition)

  private val mockPostTransitionPhaseConfig: PhaseConfig = mock[PhaseConfig]
  when(mockPostTransitionPhaseConfig.phase).thenReturn(Phase.PostTransition)

  "PreRequisitesDomain" - {

    "can be parsed from user answers" - {
      "when using same UCR for all items in Transition" in {

        val userAnswers = emptyUserAnswers
          .setValue(DeclarationTypePage, arbitrary[String](arbitraryNonTIRDeclarationType).sample.value)
          .setValue(SameUcrYesNoPage, true)
          .setValue(UniqueConsignmentReferencePage, ucr)
          .setValue(TransportedToSameCountryYesNoPage, true)
          .setValue(ItemsDestinationCountryPage, itemsDestinationCountry)
          .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

        val expectedResult = PreRequisitesDomain(
          ucr = Some(ucr),
          countryOfDispatch = None,
          countryOfDestination = None,
          itemsDestinationCountry = Some(itemsDestinationCountry),
          containerIndicator = OptionalBoolean.yes
        )

        val result = PreRequisitesDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(userAnswers)

        result.value.value mustBe expectedResult
        result.value.pages mustBe Seq(
          SameUcrYesNoPage,
          UniqueConsignmentReferencePage,
          TransportedToSameCountryYesNoPage,
          ItemsDestinationCountryPage,
          ContainerIndicatorPage
        )
      }

      "when TIR declaration type" - {
        "when using the same country of dispatch in Transition" in {
          val userAnswers = emptyUserAnswers
            .setValue(DeclarationTypePage, TIR)
            .setValue(SameUcrYesNoPage, false)
            .setValue(SameCountryOfDispatchYesNoPage, true)
            .setValue(CountryOfDispatchPage, country)
            .setValue(TransportedToSameCountryYesNoPage, true)
            .setValue(ItemsDestinationCountryPage, itemsDestinationCountry)
            .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

          val expectedResult = PreRequisitesDomain(
            ucr = None,
            countryOfDispatch = Some(country),
            countryOfDestination = None,
            itemsDestinationCountry = Some(itemsDestinationCountry),
            containerIndicator = OptionalBoolean.yes
          )

          val result = PreRequisitesDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            SameUcrYesNoPage,
            SameCountryOfDispatchYesNoPage,
            CountryOfDispatchPage,
            TransportedToSameCountryYesNoPage,
            ItemsDestinationCountryPage,
            ContainerIndicatorPage
          )
        }
        "when using the same country of  in Post Transition" in {
          val userAnswers = emptyUserAnswers
            .setValue(DeclarationTypePage, TIR)
            .setValue(SameUcrYesNoPage, false)
            .setValue(SameCountryOfDispatchYesNoPage, true)
            .setValue(CountryOfDispatchPage, country)
            .setValue(TransportedToSameCountryYesNoPage, true)
            .setValue(ItemsDestinationCountryPage, itemsDestinationCountry)
            .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

          val expectedResult = PreRequisitesDomain(
            ucr = None,
            countryOfDispatch = Some(country),
            countryOfDestination = None,
            itemsDestinationCountry = Some(itemsDestinationCountry),
            containerIndicator = OptionalBoolean.yes
          )

          val result = PreRequisitesDomain.userAnswersReader(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            SameUcrYesNoPage,
            SameCountryOfDispatchYesNoPage,
            CountryOfDispatchPage,
            TransportedToSameCountryYesNoPage,
            ItemsDestinationCountryPage,
            ContainerIndicatorPage
          )
        }

        "when not using the same country of dispatch in Transition" in {
          val userAnswers = emptyUserAnswers
            .setValue(DeclarationTypePage, TIR)
            .setValue(SameUcrYesNoPage, false)
            .setValue(SameCountryOfDispatchYesNoPage, false)
            .setValue(AddCountryOfDestinationPage, OptionalBoolean.yes)
            .setValue(TransportedToSameCountryYesNoPage, true)
            .setValue(ItemsDestinationCountryPage, itemsDestinationCountry)
            .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

          val expectedResult = PreRequisitesDomain(
            ucr = None,
            countryOfDispatch = None,
            countryOfDestination = None,
            itemsDestinationCountry = Some(itemsDestinationCountry),
            containerIndicator = OptionalBoolean.yes
          )

          val result = PreRequisitesDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            SameUcrYesNoPage,
            SameCountryOfDispatchYesNoPage,
            TransportedToSameCountryYesNoPage,
            ItemsDestinationCountryPage,
            ContainerIndicatorPage
          )
        }

        "when not using the same country of dispatch in Post Transition" - {
          "goods transport to another country after transit" in {
            val userAnswers = emptyUserAnswers
              .setValue(DeclarationTypePage, TIR)
              .setValue(SameUcrYesNoPage, false)
              .setValue(SameCountryOfDispatchYesNoPage, false)
              .setValue(AddCountryOfDestinationPage, OptionalBoolean.yes)
              .setValue(TransportedToSameCountryYesNoPage, true)
              .setValue(ItemsDestinationCountryPage, itemsDestinationCountry)
              .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

            val expectedResult = PreRequisitesDomain(
              ucr = None,
              countryOfDispatch = None,
              countryOfDestination = Some(OptionalBoolean.yes),
              itemsDestinationCountry = Some(itemsDestinationCountry),
              containerIndicator = OptionalBoolean.yes
            )

            val result = PreRequisitesDomain.userAnswersReader(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

            result.value.value mustBe expectedResult
            result.value.pages mustBe Seq(
              SameUcrYesNoPage,
              SameCountryOfDispatchYesNoPage,
              AddCountryOfDestinationPage,
              TransportedToSameCountryYesNoPage,
              ItemsDestinationCountryPage,
              ContainerIndicatorPage
            )
          }

          "goods NOT transport to another country after transit" in {
            for (selection <- Seq(OptionalBoolean.no, OptionalBoolean.maybe)) {
              val userAnswers = emptyUserAnswers
                .setValue(DeclarationTypePage, TIR)
                .setValue(SameUcrYesNoPage, false)
                .setValue(SameCountryOfDispatchYesNoPage, false)
                .setValue(AddCountryOfDestinationPage, selection)
                .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

              val expectedResult = PreRequisitesDomain(
                ucr = None,
                countryOfDispatch = None,
                countryOfDestination = Some(selection),
                itemsDestinationCountry = None,
                containerIndicator = OptionalBoolean.yes
              )

              val result = PreRequisitesDomain.userAnswersReader(mockPostTransitionPhaseConfig).apply(Nil).run(userAnswers)

              result.value.value mustBe expectedResult
              result.value.pages mustBe Seq(
                SameUcrYesNoPage,
                SameCountryOfDispatchYesNoPage,
                AddCountryOfDestinationPage,
                ContainerIndicatorPage
              )
            }
          }
        }
      }

      "when not TIR declaration type in Transition" in {
        val userAnswers = emptyUserAnswers
          .setValue(DeclarationTypePage, T1)
          .setValue(SameUcrYesNoPage, false)
          .setValue(TransportedToSameCountryYesNoPage, true)
          .setValue(ItemsDestinationCountryPage, itemsDestinationCountry)
          .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

        val expectedResult = PreRequisitesDomain(
          ucr = None,
          countryOfDispatch = None,
          countryOfDestination = None,
          itemsDestinationCountry = Some(itemsDestinationCountry),
          containerIndicator = OptionalBoolean.yes
        )

        val result = PreRequisitesDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(userAnswers)

        result.value.value mustBe expectedResult
        result.value.pages mustBe Seq(
          SameUcrYesNoPage,
          TransportedToSameCountryYesNoPage,
          ItemsDestinationCountryPage,
          ContainerIndicatorPage
        )
      }

      "can not be parsed from user answers" - {
        "when answers are empty" in {
          val result = PreRequisitesDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(emptyUserAnswers)

          result.left.value.page mustBe SameUcrYesNoPage
          result.left.value.pages mustBe Seq(
            SameUcrYesNoPage
          )
        }

        "when mandatory page is missing" - {
          "when TIR" - {
            "Transition" in {
              val mandatoryPages: Seq[QuestionPage[?]] = Seq(
                SameUcrYesNoPage,
                UniqueConsignmentReferencePage,
                SameCountryOfDispatchYesNoPage,
                CountryOfDispatchPage,
                TransportedToSameCountryYesNoPage,
                ItemsDestinationCountryPage,
                ContainerIndicatorPage
              )

              val userAnswers = emptyUserAnswers
                .setValue(DeclarationTypePage, TIR)
                .setValue(SameUcrYesNoPage, true)
                .setValue(UniqueConsignmentReferencePage, ucr)
                .setValue(SameCountryOfDispatchYesNoPage, true)
                .setValue(CountryOfDispatchPage, country)
                .setValue(TransportedToSameCountryYesNoPage, true)
                .setValue(ItemsDestinationCountryPage, itemsDestinationCountry)
                .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

              mandatoryPages.map {
                mandatoryPage =>
                  val updatedAnswers = userAnswers.removeValue(mandatoryPage)

                  val result = PreRequisitesDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(updatedAnswers)

                  result.left.value.page mustBe mandatoryPage
              }
            }

            "Post Transition" - {
              "same country of dispatch" in {
                val mandatoryPages: Seq[QuestionPage[?]] = Seq(
                  SameUcrYesNoPage,
                  UniqueConsignmentReferencePage,
                  SameCountryOfDispatchYesNoPage,
                  CountryOfDispatchPage,
                  TransportedToSameCountryYesNoPage,
                  ItemsDestinationCountryPage,
                  ContainerIndicatorPage
                )

                val userAnswers = emptyUserAnswers
                  .setValue(DeclarationTypePage, TIR)
                  .setValue(SameUcrYesNoPage, true)
                  .setValue(UniqueConsignmentReferencePage, ucr)
                  .setValue(SameCountryOfDispatchYesNoPage, true)
                  .setValue(CountryOfDispatchPage, country)
                  .setValue(TransportedToSameCountryYesNoPage, true)
                  .setValue(ItemsDestinationCountryPage, itemsDestinationCountry)
                  .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

                mandatoryPages.map {
                  mandatoryPage =>
                    val updatedAnswers = userAnswers.removeValue(mandatoryPage)

                    val result = PreRequisitesDomain.userAnswersReader(mockPostTransitionPhaseConfig).apply(Nil).run(updatedAnswers)

                    result.left.value.page mustBe mandatoryPage
                }
              }

              "not same country of dispatch" - {
                "goods transport to another country after transit" in {
                  val mandatoryPages: Seq[QuestionPage[?]] = Seq(
                    SameUcrYesNoPage,
                    UniqueConsignmentReferencePage,
                    SameCountryOfDispatchYesNoPage,
                    AddCountryOfDestinationPage,
                    TransportedToSameCountryYesNoPage,
                    ItemsDestinationCountryPage,
                    ContainerIndicatorPage
                  )

                  val userAnswers = emptyUserAnswers
                    .setValue(DeclarationTypePage, TIR)
                    .setValue(SameUcrYesNoPage, true)
                    .setValue(UniqueConsignmentReferencePage, ucr)
                    .setValue(SameCountryOfDispatchYesNoPage, false)
                    .setValue(AddCountryOfDestinationPage, OptionalBoolean.yes)
                    .setValue(TransportedToSameCountryYesNoPage, true)
                    .setValue(ItemsDestinationCountryPage, itemsDestinationCountry)
                    .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

                  mandatoryPages.map {
                    mandatoryPage =>
                      val updatedAnswers = userAnswers.removeValue(mandatoryPage)

                      val result = PreRequisitesDomain.userAnswersReader(mockPostTransitionPhaseConfig).apply(Nil).run(updatedAnswers)

                      result.left.value.page mustBe mandatoryPage
                  }
                }

                "goods NOT transport to another country after transit" in {
                  for (selection <- Seq(OptionalBoolean.no, OptionalBoolean.maybe)) {
                    val mandatoryPages: Seq[QuestionPage[?]] = Seq(
                      SameUcrYesNoPage,
                      UniqueConsignmentReferencePage,
                      SameCountryOfDispatchYesNoPage,
                      AddCountryOfDestinationPage,
                      ContainerIndicatorPage
                    )

                    val userAnswers = emptyUserAnswers
                      .setValue(DeclarationTypePage, TIR)
                      .setValue(SameUcrYesNoPage, true)
                      .setValue(UniqueConsignmentReferencePage, ucr)
                      .setValue(SameCountryOfDispatchYesNoPage, false)
                      .setValue(AddCountryOfDestinationPage, selection)
                      .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

                    mandatoryPages.map {
                      mandatoryPage =>
                        val updatedAnswers = userAnswers.removeValue(mandatoryPage)

                        val result = PreRequisitesDomain.userAnswersReader(mockPostTransitionPhaseConfig).apply(Nil).run(updatedAnswers)

                        result.left.value.page mustBe mandatoryPage
                    }
                  }
                }
              }
            }
          }

          "when non-TIR" - {
            "Transition" in {
              val mandatoryPages: Seq[QuestionPage[?]] = Seq(
                SameUcrYesNoPage,
                UniqueConsignmentReferencePage,
                TransportedToSameCountryYesNoPage,
                ItemsDestinationCountryPage,
                ContainerIndicatorPage
              )

              val userAnswers = emptyUserAnswers
                .setValue(DeclarationTypePage, arbitrary[String](arbitraryNonTIRDeclarationType).sample.value)
                .setValue(SameUcrYesNoPage, true)
                .setValue(UniqueConsignmentReferencePage, ucr)
                .setValue(SameCountryOfDispatchYesNoPage, false)
                .setValue(CountryOfDispatchPage, country)
                .setValue(TransportedToSameCountryYesNoPage, true)
                .setValue(ItemsDestinationCountryPage, itemsDestinationCountry)
                .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

              mandatoryPages.map {
                mandatoryPage =>
                  val updatedAnswers = userAnswers.removeValue(mandatoryPage)

                  val result = PreRequisitesDomain.userAnswersReader(mockTransitionPhaseConfig).apply(Nil).run(updatedAnswers)

                  result.left.value.page mustBe mandatoryPage
              }
            }

            "Post Transition" in {
              val mandatoryPages: Seq[QuestionPage[?]] = Seq(
                SameUcrYesNoPage,
                UniqueConsignmentReferencePage,
                AddCountryOfDestinationPage,
                TransportedToSameCountryYesNoPage,
                ItemsDestinationCountryPage,
                ContainerIndicatorPage
              )

              val userAnswers = emptyUserAnswers
                .setValue(DeclarationTypePage, arbitrary[String](arbitraryNonTIRDeclarationType).sample.value)
                .setValue(SameUcrYesNoPage, true)
                .setValue(UniqueConsignmentReferencePage, ucr)
                .setValue(SameCountryOfDispatchYesNoPage, false)
                .setValue(CountryOfDispatchPage, country)
                .setValue(AddCountryOfDestinationPage, OptionalBoolean.yes)
                .setValue(TransportedToSameCountryYesNoPage, true)
                .setValue(ItemsDestinationCountryPage, itemsDestinationCountry)
                .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

              mandatoryPages.map {
                mandatoryPage =>
                  val updatedAnswers = userAnswers.removeValue(mandatoryPage)

                  val result = PreRequisitesDomain.userAnswersReader(mockPostTransitionPhaseConfig).apply(Nil).run(updatedAnswers)

                  result.left.value.page mustBe mandatoryPage
              }
            }
          }
        }
      }
    }
  }
}
