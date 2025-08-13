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

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.transportMeans.departure.routes
import generators.Generators
import models.journeyDomain.transportMeans.TransportMeansDepartureDomain
import models.reference.Nationality
import models.reference.transportMeans.departure.Identification
import models.{Index, Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.departure.*
import viewModels.ListItem

class DeparturesTransportMeansAnswersHelperSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "DeparturesTransportMeansAnswersHelperSpec" - {

    "when empty user answers" - {
      "must return empty list of list items" in {
        val userAnswers = emptyUserAnswers

        val helper = new DeparturesTransportMeansAnswersHelper(userAnswers, NormalMode)
        helper.listItems mustEqual Nil
      }
    }

    "when user answers populated with a complete departure transport means" in {

      forAll(arbitraryTransportMeansDepartureAnswers(emptyUserAnswers, index), arbitrary[Mode]) {
        (userAnswers, mode) =>
          val helper    = new DeparturesTransportMeansAnswersHelper(userAnswers, mode)(messages, frontendAppConfig)
          val departure = TransportMeansDepartureDomain.userAnswersReader(index).apply(Nil).run(userAnswers).value.value
          helper.listItems mustEqual Seq(
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
        val identificationType = Identification("11", "Name of a sea-going vessel")
        val userAnswers = emptyUserAnswers
          .setValue(AddIdentificationTypeYesNoPage(departureIndex), true)
          .setValue(IdentificationPage(departureIndex), identificationType)

        forAll(arbitrary[Mode]) {
          mode =>
            val helper = new DeparturesTransportMeansAnswersHelper(userAnswers, mode)(messages, frontendAppConfig)
            helper.listItems mustEqual Seq(
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

      "and identification type and identification number is defined" - {

        val identificationType   = Identification("11", "Name of a sea-going vessel")
        val identificationNumber = nonEmptyString.sample.value

        val nationality = arbitrary[Nationality].sample.value

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
            val helper = new DeparturesTransportMeansAnswersHelper(userAnswers, mode)(messages, frontendAppConfig)
            helper.listItems mustEqual Seq(
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
