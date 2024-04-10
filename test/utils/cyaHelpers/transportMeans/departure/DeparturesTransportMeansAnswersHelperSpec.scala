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
import controllers.transportMeans.departure.routes
import generators.Generators
import models.journeyDomain.transportMeans.TransportMeansDepartureDomain
import models.reference.transportMeans.departure.Identification
import models.{Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.departure.{IdentificationPage, MeansIdentificationNumberPage}
import play.api.mvc.Call
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
                changeUrl = Call("GET", "##").url,
                removeUrl = Some(Call("GET", "#").url) // TODO Should go to remove URl
              )
            )
          )
      }
    }

    "when user answers populated with an in progress departure transport means" - {
      "and identification type is defined" in {
        val identificationType = Identification("11", "Name of a sea-going vessel")
        val userAnswers = emptyUserAnswers
          .setValue(IdentificationPage(departureIndex), identificationType)

        forAll(arbitrary[Mode]) {
          mode =>
            val helper = new DeparturesTransportMeansAnswersHelper(userAnswers, mode)
            helper.listItems mustBe Seq(
              Left(
                ListItem(
                  name = identificationType.asString,
                  changeUrl = routes.MeansIdentificationNumberController.onPageLoad(userAnswers.lrn, mode, index).url,
                  removeUrl = Some(Call("GET", "#").url) // TODO Should go to remove URl
                )
              )
            )
        }
      }

      "and identification number is defined" in {
        val identificationNumber = nonEmptyString.sample.value
        val userAnswers = emptyUserAnswers
          .setValue(IdentificationPage(index), Identification("21", "Train number"))
          .setValue(MeansIdentificationNumberPage(index), identificationNumber)

        forAll(arbitrary[Mode]) {
          mode =>
            val helper = new DeparturesTransportMeansAnswersHelper(userAnswers, mode)
            helper.listItems mustBe Seq(
              Left(
                ListItem(
                  name = identificationNumber,
                  changeUrl = routes.AddVehicleCountryYesNoController.onPageLoad(userAnswers.lrn, mode, departureIndex).url,
                  removeUrl = Some(Call("GET", "#").url) // TODO Should go to remove URl
                )
              )
            )
        }
      }

      "and identification type and identification number is defined" in {
        val identificationType   = Identification("11", "Name of a sea-going vessel")
        val identificationNumber = nonEmptyString.sample.value
        val userAnswers = emptyUserAnswers
          .setValue(IdentificationPage(index), identificationType)
          .setValue(MeansIdentificationNumberPage(index), identificationNumber)

        forAll(arbitrary[Mode]) {
          mode =>
            val helper = new DeparturesTransportMeansAnswersHelper(userAnswers, mode)
            helper.listItems mustBe Seq(
              Left(
                ListItem(
                  name = s"${identificationType.asString} - $identificationNumber",
                  changeUrl = routes.AddVehicleCountryYesNoController.onPageLoad(userAnswers.lrn, mode, departureIndex).url,
                  removeUrl = Some(Call("GET", "#").url) // TODO Should go to remove URl
                )
              )
            )
        }
      }
    }
  }
}
