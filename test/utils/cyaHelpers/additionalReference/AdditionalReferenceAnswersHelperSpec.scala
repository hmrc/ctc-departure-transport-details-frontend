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

package utils.cyaHelpers.additionalReference

import base.SpecBase
import generators.Generators
import models.reference.additionalReference.AdditionalReferenceType
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalReference.AddAdditionalReferenceYesNoPage
import pages.additionalReference.index.{AddAdditionalReferenceNumberYesNoPage, AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import play.api.mvc.Call
import viewModels.ListItem

class AdditionalReferenceAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "AdditionalReferenceAnswersHelper" - {

    "listItems" - {

      "when empty user answers" - {
        "must return empty list of list items" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers

              val helper = new AdditionalReferenceAnswersHelper(userAnswers, mode)
              helper.listItems mustBe Nil
          }
        }
      }

      "when user answers populated with complete additional references" ignore {
        forAll(arbitrary[Mode], arbitrary[AdditionalReferenceType], Gen.alphaNumStr) {
          (mode, refType, additionalReferenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(AddAdditionalReferenceYesNoPage, true)
              .setValue(AdditionalReferenceTypePage(Index(0)), refType)
              .setValue(AdditionalReferenceNumberPage(Index(0)), additionalReferenceNumber)
              .setValue(AdditionalReferenceTypePage(Index(1)), refType)
              .setValue(AddAdditionalReferenceNumberYesNoPage(Index(1)), false)

            val helper = new AdditionalReferenceAnswersHelper(userAnswers, mode)
            helper.listItems mustBe Seq(
              Right(
                ListItem(
                  name = s"${refType.toString} - $additionalReferenceNumber",
                  changeUrl = controllers.additionalReference.index.routes.AdditionalReferenceTypeController.onPageLoad(userAnswers.lrn, mode, Index(0)).url,
                  removeUrl = Some(Call("GET", "#").url) // TODO update when Remove Controller added
                )
              ),
              Right(
                ListItem(
                  name = refType.toString,
                  changeUrl = controllers.additionalReference.index.routes.AdditionalReferenceTypeController.onPageLoad(userAnswers.lrn, mode, Index(1)).url,
                  removeUrl = Some(Call("GET", "#").url) // TODO update when Remove Controller added
                )
              )
            )
        }
      }
    }
  }
}
