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
import models.Mode
import models.reference.Nationality
import models.reference.transportMeans.departure.Identification
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.departure._
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions}

class DepartureTransportMeansAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "DepartureTransportMeansAnswersHelper" - {

    "departureAddTypeYesNo" - {
      "must return None" - {
        "when departureAddTypeYesNo undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DepartureTransportMeansAnswersHelper(emptyUserAnswers, mode, departureIndex)
              val result = helper.departureAddTypeYesNo
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when AddIdentificationTypeYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(AddIdentificationTypeYesNoPage(departureIndex), true)

              val helper = new DepartureTransportMeansAnswersHelper(answers, mode, departureIndex)
              val result = helper.departureAddTypeYesNo

              result mustBe Some(
                SummaryListRow(
                  key = Key("Do you want to add the type of identification?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.AddIdentificationTypeYesNoController.onPageLoad(answers.lrn, mode, departureIndex).url,
                          visuallyHiddenText = Some("if you want to add the type of identification for the departure means of transport"),
                          attributes = Map("id" -> "change-transport-means-departure-add-identification-type")
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }
    }

    "departureAddIdentificationNumber" - {
      "must return None" - {
        "when departureAddIdentificationNumber undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DepartureTransportMeansAnswersHelper(emptyUserAnswers, mode, departureIndex)
              val result = helper.departureAddIdentificationNumber
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when AddIdentificationTypeYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(AddIdentificationNumberYesNoPage(departureIndex), true)

              val helper = new DepartureTransportMeansAnswersHelper(answers, mode, departureIndex)
              val result = helper.departureAddIdentificationNumber

              result mustBe Some(
                SummaryListRow(
                  key = Key("Do you want to add an identification for this vehicle?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transportMeans.departure.routes.AddIdentificationNumberYesNoController
                            .onPageLoad(answers.lrn, mode, departureIndex)
                            .url,
                          visuallyHiddenText = Some("if you want to add an identification for the departure means of transport"),
                          attributes = Map("id" -> "change-transport-means-departure-add-identification-number")
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }
    }

    "departureAddNationality" - {
      "must return None" - {
        "when departureAddNationality undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DepartureTransportMeansAnswersHelper(emptyUserAnswers, mode, departureIndex)
              val result = helper.departureAddNationality
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when AddIdentificationTypeYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(AddVehicleCountryYesNoPage(departureIndex), true)

              val helper = new DepartureTransportMeansAnswersHelper(answers, mode, departureIndex)
              val result = helper.departureAddNationality

              result mustBe Some(
                SummaryListRow(
                  key = Key("Do you want to add the registered country for this vehicle?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transportMeans.departure.routes.AddVehicleCountryYesNoController.onPageLoad(answers.lrn, mode, departureIndex).url,
                          visuallyHiddenText = Some("if you want to add the registered country for the departure means of transport"),
                          attributes = Map("id" -> "change-transport-means-departure-add-nationality")
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }
    }

    "departureIdentificationType" - {
      "must return None" - {
        "when departureIdentificationPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DepartureTransportMeansAnswersHelper(emptyUserAnswers, mode, departureIndex)
              val result = helper.departureIdentificationType
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when departureIdentificationPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[Identification]) {
            (mode, departureIdentification) =>
              val answers = emptyUserAnswers.setValue(IdentificationPage(departureIndex), departureIdentification)
              val helper  = new DepartureTransportMeansAnswersHelper(answers, mode, departureIndex)
              val result  = helper.departureIdentificationType

              result mustBe Some(
                SummaryListRow(
                  key = Key("Identification type".toText),
                  value = Value(departureIdentification.asString.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transportMeans.departure.routes.IdentificationController.onPageLoad(answers.lrn, mode, departureIndex).url,
                          visuallyHiddenText = Some("identification type for the departure means of transport"),
                          attributes = Map("id" -> "change-transport-means-departure-identification")
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }
    }

    "departureIdentificationNumber" - {
      "must return None" - {
        "when departureIdentificationNumberPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DepartureTransportMeansAnswersHelper(emptyUserAnswers, mode, departureIndex)
              val result = helper.departureIdentificationNumber
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when departureIdentificationNumberPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, identificationNumber) =>
              val answers = emptyUserAnswers.setValue(MeansIdentificationNumberPage(departureIndex), identificationNumber)
              val helper  = new DepartureTransportMeansAnswersHelper(answers, mode, departureIndex)
              val result  = helper.departureIdentificationNumber

              result mustBe Some(
                SummaryListRow(
                  key = Key("Identification".toText),
                  value = Value(s"$identificationNumber".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href =
                            controllers.transportMeans.departure.routes.MeansIdentificationNumberController.onPageLoad(answers.lrn, mode, departureIndex).url,
                          visuallyHiddenText = Some("identification for the departure means of transport"),
                          attributes = Map("id" -> "change-transport-means-departure-identification-number")
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }
    }

    "departureNationality" - {
      "must return None" - {
        "when VehicleCountryPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new DepartureTransportMeansAnswersHelper(emptyUserAnswers, mode, departureIndex)
              val result = helper.departureNationality
              result mustBe None
          }
        }
      }

      "must return Some(Row)" - {
        "when VehicleCountryPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[Nationality]) {
            (mode, nationality) =>
              val answers = emptyUserAnswers.setValue(VehicleCountryPage(departureIndex), nationality)
              val helper  = new DepartureTransportMeansAnswersHelper(answers, mode, departureIndex)
              val result  = helper.departureNationality

              result mustBe Some(
                SummaryListRow(
                  key = Key("Registered country".toText),
                  value = Value(s"$nationality".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transportMeans.departure.routes.VehicleCountryController.onPageLoad(answers.lrn, mode, departureIndex).url,
                          visuallyHiddenText = Some("registered country for the departure means of transport"),
                          attributes = Map("id" -> "change-transport-means-departure-vehicle-nationality")
                        )
                      )
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
