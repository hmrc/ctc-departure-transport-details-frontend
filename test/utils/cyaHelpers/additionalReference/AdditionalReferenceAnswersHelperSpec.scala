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
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalReference.AddAdditionalReferenceYesNoPage
import pages.additionalReference.index.AdditionalReferenceTypePage

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
        forAll(arbitrary[Mode], Gen.alphaNumStr) {
          (mode, additionalReferenceNumber) =>
            val refType    = arbitraryAdditionalReference.arbitrary.sample.value

            val userAnswers = emptyUserAnswers
              .setValue(AddAdditionalReferenceYesNoPage, true)
              .setValue(AdditionalReferenceTypePage(Index(0)), refType)
              .setValue(AdditionalReferenceNumberPage(Index(0)), additionalReferenceNumber)
              .setValue(AdditionalReferencePage(itemIndex, Index(1)), nonC658OrC658Document)
              .setValue(AddAdditionalReferenceNumberYesNoPage(itemIndex, Index(1)), false)

            val helper = new AdditionalReferenceAnswersHelper(userAnswers, mode, itemIndex)
            helper.listItems mustBe Seq(
              Right(
                ListItem(
                  name = s"${c651OrC658Document.toString} - $additionalReferenceNumber",
                  changeUrl = routes.AdditionalReferenceController.onPageLoad(userAnswers.lrn, mode, itemIndex, Index(0)).url,
                  removeUrl = Some(routes.RemoveAdditionalReferenceController.onPageLoad(userAnswers.lrn, mode, itemIndex, Index(0)).url)
                )
              ),
              Right(
                ListItem(
                  name = nonC658OrC658Document.toString,
                  changeUrl = routes.AdditionalReferenceController.onPageLoad(userAnswers.lrn, mode, itemIndex, Index(1)).url,
                  removeUrl = Some(routes.RemoveAdditionalReferenceController.onPageLoad(userAnswers.lrn, mode, itemIndex, Index(1)).url)
                )
              )
            )
        }
      }
    }
  }
}
