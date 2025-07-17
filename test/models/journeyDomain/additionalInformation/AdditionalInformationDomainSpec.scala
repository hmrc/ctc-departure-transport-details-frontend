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

package models.journeyDomain.additionalInformation

import base.SpecBase
import generators.Generators
import models.Index
import models.reference.additionalInformation.AdditionalInformationCode
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalInformation.index.{AddCommentsYesNoPage, AdditionalInformationTextPage, AdditionalInformationTypePage}

class AdditionalInformationDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Additional Information Domain" - {

    "can be read from user answers" - {

      "when all questions answered" in {
        forAll(arbitrary[AdditionalInformationCode], nonEmptyString) {
          (`type`, additionalInformation) =>
            val userAnswers = emptyUserAnswers
              .setValue(AdditionalInformationTypePage(additionalInformationIndex), `type`)
              .setValue(AddCommentsYesNoPage(additionalInformationIndex), true)
              .setValue(AdditionalInformationTextPage(additionalInformationIndex), additionalInformation)

            val expectedResult = AdditionalInformationDomain(
              `type` = `type`,
              value = Some(additionalInformation)
            )(additionalInformationIndex)

            val result = AdditionalInformationDomain.userAnswersReader(additionalInformationIndex).apply(Nil).run(userAnswers)

            result.value.value mustEqual expectedResult
            result.value.pages mustEqual Seq(
              AdditionalInformationTypePage(additionalInformationIndex),
              AddCommentsYesNoPage(additionalInformationIndex),
              AdditionalInformationTextPage(additionalInformationIndex)
            )
        }
      }
    }

    "can not be read from user answers" - {

      "when reference type unanswered" in {
        val result = AdditionalInformationDomain.userAnswersReader(additionalInformationIndex).apply(Nil).run(emptyUserAnswers)

        result.left.value.page mustEqual AdditionalInformationTypePage(additionalInformationIndex)
        result.left.value.pages mustEqual Seq(
          AdditionalInformationTypePage(additionalInformationIndex)
        )
      }

      "when reference type already added without a reference number" - {
        "and add additional reference number is yes" in {
          forAll(arbitrary[AdditionalInformationCode]) {
            `type` =>
              val userAnswers = emptyUserAnswers
                .setValue(AdditionalInformationTypePage(Index(0)), `type`)
                .setValue(AddCommentsYesNoPage(Index(0)), true)

              val result = AdditionalInformationDomain.userAnswersReader(Index(0)).apply(Nil).run(userAnswers)

              result.left.value.page mustEqual AdditionalInformationTextPage(Index(0))
              result.left.value.pages mustEqual Seq(
                AdditionalInformationTypePage(Index(0)),
                AddCommentsYesNoPage(Index(0)),
                AdditionalInformationTextPage(Index(0))
              )
          }
        }
      }
    }
  }
}
