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

package utils.cyaHelpers.transportMeans.active

import base.SpecBase
import controllers.transportMeans.active.routes
import generators.Generators
import models.Mode
import models.reference.transportMeans.active.Identification
import models.reference.{CustomsOffice, Nationality}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.active.*
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions}

class ActiveBorderTransportAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "ActiveBorderTransportAnswersHelper" - {

    "apply" - {

      val userAnswers = emptyUserAnswers
        .setValue(AddIdentificationYesNoPage(index), arbitrary[Boolean].sample.value)
        .setValue(IdentificationPage(index), arbitrary[Identification].sample.value)
        .setValue(AddVehicleIdentificationNumberYesNoPage(index), arbitrary[Boolean].sample.value)
        .setValue(IdentificationNumberPage(index), Gen.alphaNumStr.sample.value)
        .setValue(AddNationalityYesNoPage(index), arbitrary[Boolean].sample.value)
        .setValue(NationalityPage(index), arbitrary[Nationality].sample.value)
        .setValue(CustomsOfficeActiveBorderPage(index), arbitrary[CustomsOffice].sample.value)
        .setValue(ConveyanceReferenceNumberYesNoPage(index), arbitrary[Boolean].sample.value)
        .setValue(ConveyanceReferenceNumberPage(index), Gen.alphaNumStr.sample.value)

      "must render rows in correct order" in {
        forAll(arbitrary[Mode]) {
          mode =>
            val result = ActiveBorderTransportAnswersHelper(userAnswers, mode, index)(messages, frontendAppConfig)

            result.head.key.value mustEqual "Do you want to add the type of identification?"
            result(1).key.value mustEqual "Identification type"
            result(2).key.value mustEqual "Do you want to add an identification for this vehicle?"
            result(3).key.value mustEqual "Identification"
            result(4).key.value mustEqual "Do you want to add the registered country for this vehicle?"
            result(5).key.value mustEqual "Registered country"
            result(6).key.value mustEqual "Customs office"
            result(7).key.value mustEqual "Do you want to add a conveyance reference number?"
            result(8).key.value mustEqual "Conveyance reference number"
        }
      }
    }

    "activeBorderIdentificationType" - {
      "must return None" - {
        "when ActiveBorderIdentificationTypePage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new ActiveBorderTransportAnswersHelper(emptyUserAnswers, mode, index)
              val result = helper.activeBorderIdentificationType
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when ActiveBorderIdentificationTypePage is defined" in {
          forAll(arbitrary[Mode], arbitrary[Identification]) {
            (mode, activeIdentification) =>
              val answers = emptyUserAnswers.setValue(IdentificationPage(index), activeIdentification)
              val helper  = new ActiveBorderTransportAnswersHelper(answers, mode, index)
              val result  = helper.activeBorderIdentificationType

              result.value mustEqual
                SummaryListRow(
                  key = Key("Identification type".toText),
                  value = Value(activeIdentification.asString.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.IdentificationController.onPageLoad(answers.lrn, mode, index).url,
                          visuallyHiddenText = Some("identification type for the border means of transport"),
                          attributes = Map("id" -> "change-transport-means-active-identification")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "activeBorderIdentificationNumber" - {
      "must return None" - {
        "when ActiveBorderIdentificationNumberPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new ActiveBorderTransportAnswersHelper(emptyUserAnswers, mode, index)
              val result = helper.activeBorderIdentificationNumber
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when ActiveBorderIdentificationNumberPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, identificationNumber) =>
              val answers = emptyUserAnswers.setValue(IdentificationNumberPage(index), identificationNumber)
              val helper  = new ActiveBorderTransportAnswersHelper(answers, mode, index)
              val result  = helper.activeBorderIdentificationNumber

              result.value mustEqual
                SummaryListRow(
                  key = Key("Identification".toText),
                  value = Value(identificationNumber.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.IdentificationNumberController.onPageLoad(answers.lrn, mode, index).url,
                          visuallyHiddenText = Some("identification for the border means of transport"),
                          attributes = Map("id" -> "change-transport-means-active-identification-number")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "activeBorderAddNationality" - {
      "must return None" - {
        "when addNationality is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new ActiveBorderTransportAnswersHelper(emptyUserAnswers, mode, index)
              val result = helper.activeBorderAddNationality
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when addNationality is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddNationalityYesNoPage(index), true)
              val helper  = new ActiveBorderTransportAnswersHelper(answers, mode, index)
              val result  = helper.activeBorderAddNationality

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add the registered country for this vehicle?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.AddNationalityYesNoController.onPageLoad(answers.lrn, mode, index).url,
                          visuallyHiddenText = Some("if you want to add the registered country for the border means of transport"),
                          attributes = Map("id" -> "change-add-transport-means-vehicle-nationality")
                        )
                      )
                    )
                  )
                )
          }
        }
      }

    }

    "activeBorderAddIdentificationType" - {
      "must return None" - {
        "when add Identification Type is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new ActiveBorderTransportAnswersHelper(emptyUserAnswers, mode, index)
              val result = helper.activeBorderAddIdentificationType
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when add Identification Type is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddIdentificationYesNoPage(index), true)
              val helper  = new ActiveBorderTransportAnswersHelper(answers, mode, index)
              val result  = helper.activeBorderAddIdentificationType

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add the type of identification?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.AddIdentificationYesNoController.onPageLoad(answers.lrn, mode, index).url,
                          visuallyHiddenText = Some("if you want to add the type of identification for the border means of transport"),
                          attributes = Map("id" -> "change-add-transport-means-identification-type")
                        )
                      )
                    )
                  )
                )
          }
        }
      }

    }

    "activeBorderAddIdentificationNumber" - {
      "must return None" - {
        "when add Identification is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new ActiveBorderTransportAnswersHelper(emptyUserAnswers, mode, index)
              val result = helper.activeBorderAddIdentificationNumber
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when add Identification is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddVehicleIdentificationNumberYesNoPage(index), true)
              val helper  = new ActiveBorderTransportAnswersHelper(answers, mode, index)
              val result  = helper.activeBorderAddIdentificationNumber

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add an identification for this vehicle?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.AddVehicleIdentificationNumberYesNoController.onPageLoad(answers.lrn, mode, index).url,
                          visuallyHiddenText = Some("if you want to add an identification for the border means of transport"),
                          attributes = Map("id" -> "change-add-transport-means-identification-number")
                        )
                      )
                    )
                  )
                )
          }
        }
      }

    }

    "activeBorderNationality" - {
      "must return None" - {
        "when NationalityPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new ActiveBorderTransportAnswersHelper(emptyUserAnswers, mode, index)
              val result = helper.activeBorderNationality
              result must not be defined
          }
        }

        "must return Some(Row)" - {
          "when NationalityPage is defined" in {
            forAll(arbitrary[Mode], arbitrary[Nationality]) {
              (mode, nationality) =>
                val answers = emptyUserAnswers.setValue(NationalityPage(index), nationality)
                val helper  = new ActiveBorderTransportAnswersHelper(answers, mode, index)
                val result  = helper.activeBorderNationality

                result.value mustEqual
                  SummaryListRow(
                    key = Key("Registered country".toText),
                    value = Value(nationality.toString.toText),
                    actions = Some(
                      Actions(
                        items = List(
                          ActionItem(
                            content = "Change".toText,
                            href = routes.NationalityController.onPageLoad(answers.lrn, mode, index).url,
                            visuallyHiddenText = Some("registered country for the border means of transport"),
                            attributes = Map("id" -> "change-transport-means-active-vehicle-nationality")
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

    "customsOfficeAtBorder" - {
      "must return None" - {
        "when CustomsOfficeActiveBorderPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new ActiveBorderTransportAnswersHelper(emptyUserAnswers, mode, index)
              val result = helper.customsOfficeAtBorder
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when CustomsOfficeActiveBorderPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[CustomsOffice]) {
            (mode, customsOffice) =>
              val answers = emptyUserAnswers.setValue(CustomsOfficeActiveBorderPage(index), customsOffice)
              val helper  = new ActiveBorderTransportAnswersHelper(answers, mode, index)
              val result  = helper.customsOfficeAtBorder

              result.value mustEqual
                SummaryListRow(
                  key = Key("Customs office".toText),
                  value = Value(s"$customsOffice".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.CustomsOfficeActiveBorderController.onPageLoad(answers.lrn, mode, index).url,
                          visuallyHiddenText = Some("customs office for the border means of transport"),
                          attributes = Map("id" -> "change-transport-means-customs-office-at-border")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "activeBorderConveyanceReferenceNumberYesNo" - {
      "must return None" - {
        "when ActiveBorderConveyanceReferenceNumberYesNoPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new ActiveBorderTransportAnswersHelper(emptyUserAnswers, mode, index)
              val result = helper.activeBorderConveyanceReferenceNumberYesNo
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when activeBorderConveyanceReferenceNumberYesNoPage is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(ConveyanceReferenceNumberYesNoPage(index), true)
              val helper  = new ActiveBorderTransportAnswersHelper(answers, mode, index)
              val result  = helper.activeBorderConveyanceReferenceNumberYesNo

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add a conveyance reference number?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.ConveyanceReferenceNumberYesNoController.onPageLoad(answers.lrn, mode, index).url,
                          visuallyHiddenText = Some("if you want to add a conveyance reference number for the border means of transport"),
                          attributes = Map("id" -> "change-add-transport-means-conveyance-reference-number")
                        )
                      )
                    )
                  )
                )
          }
        }
      }

    }

    "conveyanceReferenceNumber" - {
      "must return None" - {
        "when ConveyanceReferenceNumberPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new ActiveBorderTransportAnswersHelper(emptyUserAnswers, mode, index)
              val result = helper.conveyanceReferenceNumber
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when ConveyanceReferenceNumberPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, referenceNumber) =>
              val answers = emptyUserAnswers.setValue(ConveyanceReferenceNumberPage(index), referenceNumber)
              val helper  = new ActiveBorderTransportAnswersHelper(answers, mode, index)
              val result  = helper.conveyanceReferenceNumber

              result.value mustEqual
                SummaryListRow(
                  key = Key("Conveyance reference number".toText),
                  value = Value(referenceNumber.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.ConveyanceReferenceNumberController.onPageLoad(answers.lrn, mode, index).url,
                          visuallyHiddenText = Some("conveyance reference number for the border means of transport"),
                          attributes = Map("id" -> "change-transport-means-conveyance-reference-number")
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
