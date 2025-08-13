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

package utils.cyaHelpers.additionalInformation

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.reference.additionalInformation.AdditionalInformationCode
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalInformation.AddAdditionalInformationYesNoPage
import pages.additionalInformation.index.{AddCommentsYesNoPage, AdditionalInformationTextPage, AdditionalInformationTypePage}
import viewModels.ListItem

class AdditionalInformationAnswersHelperSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "AdditionalReferenceAnswersHelper" - {

    "listItems" - {

      "when empty user answers" - {
        "must return empty list of list items" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers

              val helper = new AdditionalInformationAnswersHelper(userAnswers, mode)
              helper.listItems mustEqual Nil
          }
        }
      }

      "when user answers populated with complete additional information" in {
        forAll(arbitrary[Mode], arbitrary[AdditionalInformationCode], Gen.alphaNumStr) {
          (mode, refType, additionalInformation) =>
            val userAnswers = emptyUserAnswers
              .setValue(AddAdditionalInformationYesNoPage, true)
              .setValue(AdditionalInformationTypePage(Index(0)), refType)
              .setValue(AddCommentsYesNoPage(Index(0)), true)
              .setValue(AdditionalInformationTextPage(Index(0)), additionalInformation)

            val helper = new AdditionalInformationAnswersHelper(userAnswers, mode)
            helper.listItems mustEqual Seq(
              Right(
                ListItem(
                  name = s"${refType.toString} - $additionalInformation",
                  changeUrl =
                    controllers.additionalInformation.index.routes.AdditionalInformationTypeController.onPageLoad(Index(0), userAnswers.lrn, mode).url,
                  removeUrl = Some(
                    controllers.additionalInformation.index.routes.RemoveAdditionalInformationYesNoController.onPageLoad(userAnswers.lrn, Index(0), mode).url
                  )
                )
              )
            )
        }
      }
    }
  }
}
