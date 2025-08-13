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
import controllers.equipment.index.seals.routes
import generators.Generators
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.equipment.index.AddSealYesNoPage
import pages.equipment.index.seals.IdentificationNumberPage
import viewModels.ListItem

class SealsAnswersHelperSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "SealsAnswersHelper" - {

    "listItems" - {

      "when empty user answers" - {
        "must return empty list of list items" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers

              val helper = new SealsAnswersHelper(userAnswers, mode, equipmentIndex)
              helper.listItems mustEqual Nil
          }
        }
      }

      "when multiple seals" - {
        "and add seal yes/no page is defined (i.e. section is optional)" - {
          "must return list items with remove links" in {
            forAll(arbitrary[Mode], Gen.alphaNumStr) {
              (mode, sealId) =>
                val userAnswers = emptyUserAnswers
                  .setValue(AddSealYesNoPage(equipmentIndex), true)
                  .setValue(IdentificationNumberPage(equipmentIndex, Index(0)), sealId)
                  .setValue(IdentificationNumberPage(equipmentIndex, Index(1)), sealId)

                val helper = new SealsAnswersHelper(userAnswers, mode, equipmentIndex)
                helper.listItems mustEqual Seq(
                  Right(
                    ListItem(
                      name = sealId,
                      changeUrl = routes.IdentificationNumberController.onPageLoad(userAnswers.lrn, mode, equipmentIndex, Index(0)).url,
                      removeUrl = Some(routes.RemoveSealYesNoController.onPageLoad(userAnswers.lrn, mode, equipmentIndex, Index(0)).url)
                    )
                  ),
                  Right(
                    ListItem(
                      name = sealId,
                      changeUrl = routes.IdentificationNumberController.onPageLoad(userAnswers.lrn, mode, equipmentIndex, Index(1)).url,
                      removeUrl = Some(routes.RemoveSealYesNoController.onPageLoad(userAnswers.lrn, mode, equipmentIndex, Index(1)).url)
                    )
                  )
                )
            }
          }
        }

        "and add seal yes/no page is undefined (i.e. section is mandatory)" - {
          "must return list items with remove links" in {
            forAll(arbitrary[Mode], Gen.alphaNumStr) {
              (mode, sealId) =>
                val userAnswers = emptyUserAnswers
                  .setValue(IdentificationNumberPage(equipmentIndex, Index(0)), sealId)
                  .setValue(IdentificationNumberPage(equipmentIndex, Index(1)), sealId)

                val helper = new SealsAnswersHelper(userAnswers, mode, equipmentIndex)
                helper.listItems mustEqual Seq(
                  Right(
                    ListItem(
                      name = sealId,
                      changeUrl = routes.IdentificationNumberController.onPageLoad(userAnswers.lrn, mode, equipmentIndex, Index(0)).url,
                      removeUrl = Some(routes.RemoveSealYesNoController.onPageLoad(userAnswers.lrn, mode, equipmentIndex, Index(0)).url)
                    )
                  ),
                  Right(
                    ListItem(
                      name = sealId,
                      changeUrl = routes.IdentificationNumberController.onPageLoad(userAnswers.lrn, mode, equipmentIndex, Index(1)).url,
                      removeUrl = Some(routes.RemoveSealYesNoController.onPageLoad(userAnswers.lrn, mode, equipmentIndex, Index(1)).url)
                    )
                  )
                )
            }
          }
        }
      }

      "when one seal" - {
        "and add seal yes/no page is defined (i.e. section is optional)" - {
          "must return list item with remove link" in {
            forAll(arbitrary[Mode], Gen.alphaNumStr) {
              (mode, sealId) =>
                val userAnswers = emptyUserAnswers
                  .setValue(AddSealYesNoPage(equipmentIndex), true)
                  .setValue(IdentificationNumberPage(equipmentIndex, Index(0)), sealId)

                val helper = new SealsAnswersHelper(userAnswers, mode, equipmentIndex)
                helper.listItems mustEqual Seq(
                  Right(
                    ListItem(
                      name = sealId,
                      changeUrl = routes.IdentificationNumberController.onPageLoad(userAnswers.lrn, mode, equipmentIndex, Index(0)).url,
                      removeUrl = Some(routes.RemoveSealYesNoController.onPageLoad(userAnswers.lrn, mode, equipmentIndex, Index(0)).url)
                    )
                  )
                )
            }
          }
        }

        "and add seal yes/no page is undefined (i.e. section is mandatory)" - {
          "must return list item without remove link" in {
            forAll(arbitrary[Mode], Gen.alphaNumStr) {
              (mode, sealId) =>
                val userAnswers = emptyUserAnswers
                  .setValue(IdentificationNumberPage(equipmentIndex, Index(0)), sealId)

                val helper = new SealsAnswersHelper(userAnswers, mode, equipmentIndex)
                helper.listItems mustEqual Seq(
                  Right(
                    ListItem(
                      name = sealId,
                      changeUrl = routes.IdentificationNumberController.onPageLoad(userAnswers.lrn, mode, equipmentIndex, Index(0)).url,
                      removeUrl = None
                    )
                  )
                )
            }
          }
        }
      }
    }
  }

}
