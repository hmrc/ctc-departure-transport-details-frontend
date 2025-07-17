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

package models.journeyDomain.additionalReferences

import base.SpecBase
import generators.Generators
import models.Index
import models.reference.additionalReference.AdditionalReferenceType
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalReference.index.{AddAdditionalReferenceNumberYesNoPage, AdditionalReferenceNumberPage, AdditionalReferenceTypePage}

class AdditionalReferenceDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Additional Reference Domain" - {

    "can be read from user answers" - {

      "when all questions answered" in {
        forAll(arbitrary[AdditionalReferenceType], nonEmptyString) {
          (`type`, number) =>
            val userAnswers = emptyUserAnswers
              .setValue(AdditionalReferenceTypePage(additionalReferenceIndex), `type`)
              .setValue(AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex), true)
              .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), number)

            val expectedResult = AdditionalReferenceDomain(
              `type` = `type`,
              number = Some(number)
            )(additionalReferenceIndex)

            val result = AdditionalReferenceDomain.userAnswersReader(additionalReferenceIndex).apply(Nil).run(userAnswers)

            result.value.value mustEqual expectedResult
            result.value.pages mustEqual Seq(
              AdditionalReferenceTypePage(additionalReferenceIndex),
              AddAdditionalReferenceNumberYesNoPage(additionalReferenceIndex),
              AdditionalReferenceNumberPage(additionalReferenceIndex)
            )
        }
      }
    }

    "can not be read from user answers" - {

      "when reference type unanswered" in {
        val result = AdditionalReferenceDomain.userAnswersReader(additionalReferenceIndex).apply(Nil).run(emptyUserAnswers)

        result.left.value.page mustEqual AdditionalReferenceTypePage(additionalReferenceIndex)
        result.left.value.pages mustEqual Seq(
          AdditionalReferenceTypePage(additionalReferenceIndex)
        )
      }

      "when reference type already added without a reference number" - {
        "and add additional reference number is yes" in {
          forAll(arbitrary[AdditionalReferenceType]) {
            `type` =>
              val userAnswers = emptyUserAnswers
                .setValue(AdditionalReferenceTypePage(Index(0)), `type`)
                .setValue(AddAdditionalReferenceNumberYesNoPage(Index(0)), true)

              val result = AdditionalReferenceDomain.userAnswersReader(Index(0)).apply(Nil).run(userAnswers)

              result.left.value.page mustEqual AdditionalReferenceNumberPage(Index(0))
              result.left.value.pages mustEqual Seq(
                AdditionalReferenceTypePage(Index(0)),
                AddAdditionalReferenceNumberYesNoPage(Index(0)),
                AdditionalReferenceNumberPage(Index(0))
              )
          }
        }
      }
    }
  }
}
