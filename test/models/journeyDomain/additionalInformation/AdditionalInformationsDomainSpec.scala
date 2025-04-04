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
import org.scalacheck.Gen
import pages.additionalInformation.AddAnotherAdditionalInformationPage
import pages.additionalInformation.index.AdditionalInformationTypePage
import pages.sections.additionalInformation.AdditionalInformationListSection

class AdditionalInformationsDomainSpec extends SpecBase with Generators {

  "AdditionalInformations" - {

    "can be parsed from UserAnswers" in {

      val numberOfAdditionalInformations = Gen.choose(1, frontendAppConfig.maxAdditionalInformation).sample.value

      val userAnswers = (0 until numberOfAdditionalInformations).foldLeft(emptyUserAnswers) {
        case (updatedUserAnswers, index) =>
          arbitraryAdditionalInformationAnswers(updatedUserAnswers, Index(index)).sample.value
      }

      val result = AdditionalInformationsDomain.userAnswersReader.apply(Nil).run(userAnswers)

      result.value.value.value.length mustBe numberOfAdditionalInformations
      result.value.pages.last mustBe AddAnotherAdditionalInformationPage
    }

    "cannot be parsed from user answers" - {
      "when no additional Information" in {
        val result = AdditionalInformationsDomain.userAnswersReader.apply(Nil).run(emptyUserAnswers)

        result.left.value.page mustBe AdditionalInformationTypePage(Index(0))
        result.left.value.pages mustBe Seq(
          AdditionalInformationTypePage(Index(0))
        )
      }
    }
  }
}
