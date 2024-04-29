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

package utils.cyaHelpers.transportMeans.departure

import base.SpecBase
import config.PhaseConfig
import controllers.transportMeans.departure.routes
import generators.Generators
import models.journeyDomain.transportMeans.TransportMeansDepartureDomain
import models.reference.Nationality
import models.reference.transportMeans.departure.Identification
import models.{Index, Mode, NormalMode, OptionalBoolean, Phase}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.preRequisites.ContainerIndicatorPage
import pages.transportMeans.AddDepartureTransportMeansYesNoPage
import pages.transportMeans.departure._
import viewModels.ListItem

class DeparturesTransportMeansAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "DeparturesTransportMeansAnswersHelperSpec" - {

    "when empty user answers" - {
      "must return empty list of list items" in {
        val userAnswers = emptyUserAnswers

        val helper = new DeparturesTransportMeansAnswersHelper(userAnswers, NormalMode)
        helper.listItems mustBe Nil
      }
    }

    "when user answers populated with a complete departure transport means" in {

      forAll(arbitraryTransportMeansDepartureAnswers(emptyUserAnswers, index)(phaseConfig), arbitrary[Mode]) {
        (userAnswers, mode) =>
          val helper    = new DeparturesTransportMeansAnswersHelper(userAnswers, mode)(messages, frontendAppConfig, phaseConfig)
          val departure = TransportMeansDepartureDomain.userAnswersReader(index).apply(Nil).run(userAnswers).value.value
          helper.listItems mustBe Seq(
            Right(
              ListItem(
                name = departure.asString,
                changeUrl = routes.DepartureTransportAnswersController.onPageLoad(userAnswers.lrn, mode, index).url,
                removeUrl = None
              )
            )
          )
      }
    }

    "when user answers populated with an in progress departure transport means" - {
      "and identification type is defined" - {
        "when transition" - {
          "when section is mandatory and there is 1 departure transport means" in {
            val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
            when(mockPhaseConfig.phase).thenReturn(Phase.Transition)
            val identificationType = Identification("11", "Name of a sea-going vessel")
            val userAnswers = emptyUserAnswers
              .setValue(IdentificationPage(departureIndex), identificationType)

            forAll(arbitrary[Mode]) {
              mode =>
                val helper = new DeparturesTransportMeansAnswersHelper(userAnswers, mode)(messages, frontendAppConfig, mockPhaseConfig)
                helper.listItems mustBe Seq(
                  Left(
                    ListItem(
                      name = s"Departure means of transport ${index.display} - ${identificationType.asString}",
                      changeUrl = routes.AddIdentificationNumberYesNoController.onPageLoad(userAnswers.lrn, mode, index).url,
                      removeUrl = None
                    )
                  )
                )
            }
          }
          "when section is mandatory and there are 2 departure transport means" in {
            val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
            when(mockPhaseConfig.phase).thenReturn(Phase.Transition)
            val identificationType = Identification("11", "Name of a sea-going vessel")
            val userAnswers = emptyUserAnswers
              .setValue(IdentificationPage(Index(0)), identificationType)
              .setValue(IdentificationPage(Index(1)), identificationType)

            forAll(arbitrary[Mode]) {
              mode =>
                val helper = new DeparturesTransportMeansAnswersHelper(userAnswers, mode)(messages, frontendAppConfig, mockPhaseConfig)
                helper.listItems mustBe Seq(
                  Left(
                    ListItem(
                      name = s"Departure means of transport ${Index(0).display} - ${identificationType.asString}",
                      changeUrl = routes.AddIdentificationNumberYesNoController.onPageLoad(userAnswers.lrn, mode, Index(0)).url,
                      removeUrl = Some(
                        controllers.transportMeans.departure.routes.RemoveDepartureMeansOfTransportYesNoController.onPageLoad(lrn, mode, Index(0)).url
                      )
                    )
                  ),
                  Left(
                    ListItem(
                      name = s"Departure means of transport ${Index(1).display} - ${identificationType.asString}",
                      changeUrl = routes.AddIdentificationNumberYesNoController.onPageLoad(userAnswers.lrn, mode, Index(1)).url,
                      removeUrl = Some(
                        controllers.transportMeans.departure.routes.RemoveDepartureMeansOfTransportYesNoController.onPageLoad(lrn, mode, Index(1)).url
                      )
                    )
                  )
                )
            }
          }

          "when section is optional" in {
            val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
            when(mockPhaseConfig.phase).thenReturn(Phase.Transition)
            val identificationType = Identification("11", "Name of a sea-going vessel")
            val userAnswers = emptyUserAnswers
              .setValue(AddDepartureTransportMeansYesNoPage, true)
              .setValue(AddIdentificationTypeYesNoPage(departureIndex), true)
              .setValue(IdentificationPage(departureIndex), identificationType)

            forAll(arbitrary[Mode]) {
              mode =>
                val helper = new DeparturesTransportMeansAnswersHelper(userAnswers, mode)(messages, frontendAppConfig, mockPhaseConfig)
                helper.listItems mustBe Seq(
                  Left(
                    ListItem(
                      name = s"Departure means of transport ${index.display} - ${identificationType.asString}",
                      changeUrl = routes.AddIdentificationNumberYesNoController.onPageLoad(userAnswers.lrn, mode, index).url,
                      removeUrl = Some(
                        controllers.transportMeans.departure.routes.RemoveDepartureMeansOfTransportYesNoController.onPageLoad(lrn, mode, departureIndex).url
                      )
                    )
                  )
                )
            }
          }

        }

        "when post transition" in {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)
          val identificationType = Identification("11", "Name of a sea-going vessel")
          val userAnswers = emptyUserAnswers
            .setValue(AddIdentificationTypeYesNoPage(departureIndex), true)
            .setValue(IdentificationPage(departureIndex), identificationType)

          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DeparturesTransportMeansAnswersHelper(userAnswers, mode)(messages, frontendAppConfig, mockPhaseConfig)
              helper.listItems mustBe Seq(
                Left(
                  ListItem(
                    name = s"Departure means of transport ${index.display} - ${identificationType.asString}",
                    changeUrl = routes.MeansIdentificationNumberController.onPageLoad(userAnswers.lrn, mode, index).url,
                    removeUrl = None
                  )
                )
              )
          }
        }

      }

      "and identification number is defined" - {
        val identificationNumber = nonEmptyString.sample.value
        "when in transition" in {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.Transition)
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
            .setValue(AddDepartureTransportMeansYesNoPage, true)
            .setValue(AddIdentificationTypeYesNoPage(index), false)
            .setValue(AddIdentificationNumberYesNoPage(index), true)
            .setValue(MeansIdentificationNumberPage(index), identificationNumber)

          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DeparturesTransportMeansAnswersHelper(userAnswers, mode)(messages, frontendAppConfig, mockPhaseConfig)
              helper.listItems mustBe Seq(
                Left(
                  ListItem(
                    name = s"Departure means of transport ${index.display} - $identificationNumber",
                    changeUrl = routes.AddVehicleCountryYesNoController.onPageLoad(userAnswers.lrn, mode, departureIndex).url,
                    removeUrl = Some(
                      controllers.transportMeans.departure.routes.RemoveDepartureMeansOfTransportYesNoController.onPageLoad(lrn, mode, departureIndex).url
                    )
                  )
                )
              )
          }
        }

      }

      "and identification type and identification number is defined" - {

        val identificationType   = Identification("11", "Name of a sea-going vessel")
        val identificationNumber = nonEmptyString.sample.value

        "when in transition" in {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.Transition)
          val userAnswers = emptyUserAnswers
            .setValue(AddDepartureTransportMeansYesNoPage, true)
            .setValue(ContainerIndicatorPage, OptionalBoolean.yes)
            .setValue(AddIdentificationTypeYesNoPage(index), true)
            .setValue(IdentificationPage(index), identificationType)
            .setValue(AddIdentificationNumberYesNoPage(index), true)
            .setValue(MeansIdentificationNumberPage(index), identificationNumber)

          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DeparturesTransportMeansAnswersHelper(userAnswers, mode)(messages, frontendAppConfig, mockPhaseConfig)
              helper.listItems mustBe Seq(
                Left(
                  ListItem(
                    name = s"Departure means of transport ${index.display} - ${identificationType.asString} - $identificationNumber",
                    changeUrl = routes.AddVehicleCountryYesNoController.onPageLoad(userAnswers.lrn, mode, departureIndex).url,
                    removeUrl = Some(
                      controllers.transportMeans.departure.routes.RemoveDepartureMeansOfTransportYesNoController.onPageLoad(lrn, mode, departureIndex).url
                    )
                  )
                )
              )
          }
        }

        "when in post transition" in {

          val nationality = arbitrary[Nationality].sample.value

          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

          val secondIndex = Index(1)
          val userAnswers = emptyUserAnswers
            .setValue(IdentificationPage(index), identificationType)
            .setValue(MeansIdentificationNumberPage(index), identificationNumber)
            .setValue(VehicleCountryPage(departureIndex), nationality)
            .setValue(IdentificationPage(secondIndex), identificationType)
            .setValue(MeansIdentificationNumberPage(secondIndex), identificationNumber)
            .setValue(VehicleCountryPage(secondIndex), nationality)

          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DeparturesTransportMeansAnswersHelper(userAnswers, mode)(messages, frontendAppConfig, mockPhaseConfig)
              helper.listItems mustBe Seq(
                Right(
                  ListItem(
                    name = s"Departure means of transport ${index.display} - ${identificationType.asString} - $identificationNumber",
                    changeUrl = routes.DepartureTransportAnswersController.onPageLoad(userAnswers.lrn, mode, departureIndex).url,
                    removeUrl = Some(
                      controllers.transportMeans.departure.routes.RemoveDepartureMeansOfTransportYesNoController.onPageLoad(lrn, mode, departureIndex).url
                    )
                  )
                ),
                Right(
                  ListItem(
                    name = s"Departure means of transport ${secondIndex.display} - ${identificationType.asString} - $identificationNumber",
                    changeUrl = routes.DepartureTransportAnswersController.onPageLoad(userAnswers.lrn, mode, secondIndex).url,
                    removeUrl = Some(
                      controllers.transportMeans.departure.routes.RemoveDepartureMeansOfTransportYesNoController.onPageLoad(lrn, mode, secondIndex).url
                    )
                  )
                )
              )
          }
        }

      }
    }
  }
}
