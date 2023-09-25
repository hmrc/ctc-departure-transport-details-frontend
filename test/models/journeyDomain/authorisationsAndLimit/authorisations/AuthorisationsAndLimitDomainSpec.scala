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
import models.authorisations.AuthorisationType
import models.domain.{EitherType, UserAnswersReader}
import models.journeyDomain.authorisationsAndLimit.limit.LimitDomain
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.authorisationsAndLimit.authorisations.index.{AuthorisationReferenceNumberPage, AuthorisationTypePage}
import pages.authorisationsAndLimit.limit.{AddLimitDateYesNoPage, LimitDatePage}
import pages.external.AdditionalDeclarationTypePage

class AuthorisationsAndLimitDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val authRefNumber = Gen.alphaNumStr.sample.value

  "AuthorisationsAndLimitDomain" - {

    "limitReader" - {

      "can be parsed from UserAnswers" - {
        "when authorisation type is not ACR" in {

          val additionalDeclarationType = Gen.oneOf("A", "D").sample.value
          val authType                  = Gen.oneOf(AuthorisationType.TRD, AuthorisationType.SSE).sample.value

          val userAnswers = emptyUserAnswers
            .setValue(AdditionalDeclarationTypePage, additionalDeclarationType)
            .setValue(AuthorisationTypePage(authorisationIndex), authType)
            .setValue(AuthorisationReferenceNumberPage(authorisationIndex), authRefNumber)

          val authorisationsDomain = AuthorisationsDomain.userAnswersReader.run(userAnswers).value

          val result: EitherType[Option[LimitDomain]] = UserAnswersReader[Option[LimitDomain]](
            AuthorisationsAndLimitDomain.limitReader(authorisationsDomain)
          ).run(userAnswers)

          result.value mustBe None
        }
      }

      "cannot be parsed from user answers" - {
        "when any AuthorisationType is ACR" - {
          "and additional declaration type is A" in {
            val authType = AuthorisationType.ACR

            val userAnswers = emptyUserAnswers
              .setValue(AdditionalDeclarationTypePage, "A")
              .setValue(AuthorisationTypePage(authorisationIndex), authType)
              .setValue(AuthorisationReferenceNumberPage(authorisationIndex), authRefNumber)

            val authorisationsDomain = AuthorisationsDomain.userAnswersReader.run(userAnswers).value

            val result: EitherType[Option[LimitDomain]] = UserAnswersReader[Option[LimitDomain]](
              AuthorisationsAndLimitDomain.limitReader(authorisationsDomain)
            ).run(userAnswers)

            result.left.value.page mustBe LimitDatePage
          }

          "and additional declaration type is D" in {
            val authType = AuthorisationType.ACR

            val userAnswers = emptyUserAnswers
              .setValue(AdditionalDeclarationTypePage, "D")
              .setValue(AuthorisationTypePage(authorisationIndex), authType)
              .setValue(AuthorisationReferenceNumberPage(authorisationIndex), authRefNumber)

            val authorisationsDomain = AuthorisationsDomain.userAnswersReader.run(userAnswers).value

            val result: EitherType[Option[LimitDomain]] = UserAnswersReader[Option[LimitDomain]](
              AuthorisationsAndLimitDomain.limitReader(authorisationsDomain)
            ).run(userAnswers)

            result.left.value.page mustBe AddLimitDateYesNoPage
          }
        }
      }
    }
  }
}
