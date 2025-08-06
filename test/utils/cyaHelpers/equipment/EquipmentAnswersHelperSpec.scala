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

package utils.cyaHelpers.equipment

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.equipment.index.routes.*
import controllers.equipment.index.seals.routes.*
import generators.Generators
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.equipment.SealSection
import pages.equipment.index.*
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.html.components.implicits.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

class EquipmentAnswersHelperSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "EquipmentAnswersHelper" - {

    "containerIdentificationNumberYesNo" - {
      "must return None" - {
        "when AddContainerIdentificationNumberYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new EquipmentAnswersHelper(emptyUserAnswers, mode, index)
              val result = helper.containerIdentificationNumberYesNo
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when AddContainerIdentificationNumberYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(AddContainerIdentificationNumberYesNoPage(index), true)

              val helper = new EquipmentAnswersHelper(answers, mode, index)
              val result = helper.containerIdentificationNumberYesNo

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add a container identification number?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = AddContainerIdentificationNumberYesNoController.onPageLoad(answers.lrn, mode, index).url,
                          visuallyHiddenText = Some("if you want to add an identification number"),
                          attributes = Map("id" -> "change-add-container-identification-number")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "containerIdentificationNumber" - {
      "must return None" - {
        "when ContainerIdentificationNumberPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new EquipmentAnswersHelper(emptyUserAnswers, mode, index)
              val result = helper.containerIdentificationNumber
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when ContainerIdentificationNumberPage defined" in {
          forAll(arbitrary[Mode], nonEmptyString) {
            (mode, containerIdentificationNumber) =>
              val answers = emptyUserAnswers
                .setValue(ContainerIdentificationNumberPage(index), containerIdentificationNumber)

              val helper = new EquipmentAnswersHelper(answers, mode, index)
              val result = helper.containerIdentificationNumber

              result.value mustEqual
                SummaryListRow(
                  key = Key("Container identification number".toText),
                  value = Value(containerIdentificationNumber.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = ContainerIdentificationNumberController.onPageLoad(answers.lrn, mode, index).url,
                          visuallyHiddenText = Some("identification number"),
                          attributes = Map("id" -> "change-container-identification-number")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "sealsYesNo" - {
      "must return None" - {
        "when AddSealYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new EquipmentAnswersHelper(emptyUserAnswers, mode, index)
              val result = helper.sealsYesNo
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when AddSealYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers
                .setValue(AddSealYesNoPage(index), true)

              val helper = new EquipmentAnswersHelper(answers, mode, index)
              val result = helper.sealsYesNo

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add a seal?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = AddSealYesNoController.onPageLoad(answers.lrn, mode, index).url,
                          visuallyHiddenText = Some("if you want to add a seal"),
                          attributes = Map("id" -> "change-add-seals")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "seal" - {
      "must return None" - {
        "when seal is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new EquipmentAnswersHelper(emptyUserAnswers, mode, equipmentIndex)
              val result = helper.seal(sealIndex)
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when seal is defined" in {
          forAll(arbitrary[Mode], nonEmptyString) {
            (mode, sealIdNumber) =>
              val userAnswers = emptyUserAnswers.setValue(seals.IdentificationNumberPage(equipmentIndex, sealIndex), sealIdNumber)
              val helper      = new EquipmentAnswersHelper(userAnswers, mode, equipmentIndex)
              val result      = helper.seal(index).get

              result.key.value mustEqual "Seal 1"
              result.value.value mustEqual sealIdNumber
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual IdentificationNumberController.onPageLoad(userAnswers.lrn, mode, equipmentIndex, sealIndex).url
              action.visuallyHiddenText.get mustEqual "seal 1"
              action.id mustEqual "change-seal-1"
          }
        }
      }
    }

    "addOrRemoveSeals" - {
      "must return None" - {
        "when seals array is empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new EquipmentAnswersHelper(emptyUserAnswers, mode, equipmentIndex)
              val result = helper.addOrRemoveSeals
              result must not be defined
          }
        }
      }

      "must return Some(Link)" - {
        "when seals array is non-empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(SealSection(equipmentIndex, Index(0)), Json.obj("foo" -> "bar"))
              val helper  = new EquipmentAnswersHelper(answers, mode, equipmentIndex)
              val result  = helper.addOrRemoveSeals.get

              result.id mustEqual "add-or-remove-seals"
              result.text mustEqual "Add or remove seals"
              result.href mustEqual AddAnotherSealController.onPageLoad(answers.lrn, mode, equipmentIndex).url
          }
        }
      }
    }
  }
}
