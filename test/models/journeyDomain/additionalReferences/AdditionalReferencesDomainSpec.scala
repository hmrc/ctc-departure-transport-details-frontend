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
import org.scalacheck.Gen
import pages.additionalReference.AddAnotherAdditionalReferencePage
import pages.additionalReference.index.AdditionalReferenceTypePage
import pages.sections.additionalReference.AdditionalReferencesSection

class AdditionalReferencesDomainSpec extends SpecBase with Generators {

  "AdditionalReferences" - {

    "can be parsed from UserAnswers" in {

      val numberOfAdditionalReferences = Gen.choose(1, frontendAppConfig.maxAdditionalReferences).sample.value

      val userAnswers = (0 until numberOfAdditionalReferences).foldLeft(emptyUserAnswers) {
        case (updatedUserAnswers, index) =>
          arbitraryAdditionalReferenceAnswers(updatedUserAnswers, Index(index)).sample.value
      }

      val result = AdditionalReferencesDomain.userAnswersReader.apply(Nil).run(userAnswers)

      result.value.value.value.length mustBe numberOfAdditionalReferences
      result.value.pages.last mustBe AddAnotherAdditionalReferencePage
    }

    "cannot be parsed from user answers" - {
      "when no additional reference" in {
        val result = AdditionalReferencesDomain.userAnswersReader.apply(Nil).run(emptyUserAnswers)

        result.left.value.page mustBe AdditionalReferenceTypePage(Index(0))
        result.left.value.pages mustBe Seq(
          AdditionalReferenceTypePage(Index(0))
        )
      }
    }
  }
}
