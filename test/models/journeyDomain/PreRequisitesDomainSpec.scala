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
import generators.Generators
import models.OptionalBoolean
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.QuestionPage
import pages.external.DeclarationTypePage
import pages.preRequisites.*

class PreRequisitesDomainSpec extends SpecBase with Generators {

  private val ucr                     = Gen.alphaNumStr.sample.value
  private val country                 = arbitrary[Country].sample.value
  private val itemsDestinationCountry = arbitrary[Country].sample.value

  "PreRequisitesDomain" - {

    "can be parsed from user answers" - {

      "when TIR declaration type" - {
        "when using the same country of dispatch" in {
          val userAnswers = emptyUserAnswers
            .setValue(DeclarationTypePage, TIR)
            .setValue(SameUcrYesNoPage, false)
            .setValue(SameCountryOfDispatchYesNoPage, true)
            .setValue(AddCountryOfDestinationPage, OptionalBoolean.yes)
            .setValue(CountryOfDispatchPage, country)
            .setValue(TransportedToSameCountryYesNoPage, true)
            .setValue(ItemsDestinationCountryPage, itemsDestinationCountry)
            .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

          val expectedResult = PreRequisitesDomain(
            ucr = None,
            countryOfDispatch = Some(country),
            countryOfDestination = Some(itemsDestinationCountry),
            containerIndicator = OptionalBoolean.yes
          )

          val result = PreRequisitesDomain.userAnswersReader.apply(Nil).run(userAnswers)

          result.value.value mustEqual expectedResult
          result.value.pages mustEqual Seq(
            SameUcrYesNoPage,
            SameCountryOfDispatchYesNoPage,
            CountryOfDispatchPage,
            AddCountryOfDestinationPage,
            TransportedToSameCountryYesNoPage,
            ItemsDestinationCountryPage,
            ContainerIndicatorPage
          )
        }

        "when not using the same country of dispatch" - {
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
              countryOfDestination = Some(itemsDestinationCountry),
              containerIndicator = OptionalBoolean.yes
            )

            val result = PreRequisitesDomain.userAnswersReader.apply(Nil).run(userAnswers)

            result.value.value mustEqual expectedResult
            result.value.pages mustEqual Seq(
              SameUcrYesNoPage,
              SameCountryOfDispatchYesNoPage,
              AddCountryOfDestinationPage,
              TransportedToSameCountryYesNoPage,
              ItemsDestinationCountryPage,
              ContainerIndicatorPage
            )
          }

          "goods NOT transport to another country after transit" in {
            forAll(Gen.oneOf(OptionalBoolean.no, OptionalBoolean.maybe)) {
              selection =>
                val userAnswers = emptyUserAnswers
                  .setValue(DeclarationTypePage, TIR)
                  .setValue(SameUcrYesNoPage, false)
                  .setValue(SameCountryOfDispatchYesNoPage, false)
                  .setValue(AddCountryOfDestinationPage, selection)
                  .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

                val expectedResult = PreRequisitesDomain(
                  ucr = None,
                  countryOfDispatch = None,
                  countryOfDestination = None,
                  containerIndicator = OptionalBoolean.yes
                )

                val result = PreRequisitesDomain.userAnswersReader.apply(Nil).run(userAnswers)

                result.value.value mustEqual expectedResult
                result.value.pages mustEqual Seq(
                  SameUcrYesNoPage,
                  SameCountryOfDispatchYesNoPage,
                  AddCountryOfDestinationPage,
                  ContainerIndicatorPage
                )
            }
          }
        }
      }

      "can not be parsed from user answers" - {
        "when answers are empty" in {
          val result = PreRequisitesDomain.userAnswersReader.apply(Nil).run(emptyUserAnswers)

          result.left.value.page mustEqual SameUcrYesNoPage
          result.left.value.pages mustEqual Seq(
            SameUcrYesNoPage
          )
        }

        "when mandatory page is missing" - {
          "when TIR" - {
            "same country of dispatch" in {
              val mandatoryPages: Seq[QuestionPage[?]] = Seq(
                SameUcrYesNoPage,
                UniqueConsignmentReferencePage,
                SameCountryOfDispatchYesNoPage,
                CountryOfDispatchPage,
                AddCountryOfDestinationPage,
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
                .setValue(AddCountryOfDestinationPage, OptionalBoolean.yes)
                .setValue(TransportedToSameCountryYesNoPage, true)
                .setValue(ItemsDestinationCountryPage, itemsDestinationCountry)
                .setValue(ContainerIndicatorPage, OptionalBoolean.yes)

              mandatoryPages.map {
                mandatoryPage =>
                  val updatedAnswers = userAnswers.removeValue(mandatoryPage)

                  val result = PreRequisitesDomain.userAnswersReader.apply(Nil).run(updatedAnswers)

                  result.left.value.page mustEqual mandatoryPage
              }
            }

            "when not same country of dispatch" - {
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

                    val result = PreRequisitesDomain.userAnswersReader.apply(Nil).run(updatedAnswers)

                    result.left.value.page mustEqual mandatoryPage
                }
              }

              "goods NOT transport to another country after transit" in {
                forAll(Gen.oneOf(OptionalBoolean.no, OptionalBoolean.maybe)) {
                  selection =>
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

                        val result = PreRequisitesDomain.userAnswersReader.apply(Nil).run(updatedAnswers)

                        result.left.value.page mustEqual mandatoryPage
                    }
                }
              }
            }
          }

          "when non-TIR" in {
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

                val result = PreRequisitesDomain.userAnswersReader.apply(Nil).run(updatedAnswers)

                result.left.value.page mustEqual mandatoryPage
            }
          }
        }
      }
    }
  }
}
