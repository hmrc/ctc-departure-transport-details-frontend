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

package models.journeyDomain.authorisationsAndLimit.authorisations

import base.SpecBase
import generators.Generators
import models.Index
import org.scalacheck.Gen
import pages.sections.authorisationsAndLimit.AuthorisationsSection

class AuthorisationsDomainSpec extends SpecBase with Generators {

  "Authorisations" - {

    "can be parsed from UserAnswers" in {

      val numberOfAuthorisations = Gen.choose(1, frontendAppConfig.maxAuthorisations).sample.value

      val userAnswers = (0 until numberOfAuthorisations).foldLeft(emptyUserAnswers)({
        case (updatedUserAnswers, index) =>
          arbitraryAuthorisationAnswers(updatedUserAnswers, Index(index)).sample.value
      })

      val result = AuthorisationsDomain.userAnswersReader.apply(Nil).run(userAnswers)

      result.value.value.authorisations.length mustBe numberOfAuthorisations
      result.value.pages.last mustBe AuthorisationsSection
    }
  }
}
