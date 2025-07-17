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
import models.reference.authorisations.AuthorisationType
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.authorisationsAndLimit.authorisations.index.{AuthorisationReferenceNumberPage, AuthorisationTypePage}
import pages.authorisationsAndLimit.limit.LimitDatePage
import pages.external.AdditionalDeclarationTypePage

class AuthorisationsAndLimitDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val authRefNumber = Gen.alphaNumStr.sample.value
  private val authTypeACR   = AuthorisationType("C521", "ACR - authorisation for the status of authorised consignor for Union transit")
  private val authTypeSSE   = AuthorisationType("C523", "SSE - authorisation for the use of seals of a special type")
  private val authTypeTRD   = AuthorisationType("C524", "TRD - authorisation to use transit declaration with a reduced dataset")

  "AuthorisationsAndLimitDomain" - {

    "limitReader" - {

      "can be parsed from UserAnswers" - {
        "when authorisation type is not ACR" in {

          val additionalDeclarationType = Gen.oneOf("A", "D").sample.value
          val authType                  = Gen.oneOf(authTypeTRD, authTypeSSE).sample.value

          val userAnswers = emptyUserAnswers
            .setValue(AdditionalDeclarationTypePage, additionalDeclarationType)
            .setValue(AuthorisationTypePage(authorisationIndex), authType)
            .setValue(AuthorisationReferenceNumberPage(authorisationIndex), authRefNumber)

          val authorisationsDomain = AuthorisationsDomain.userAnswersReader.apply(Nil).run(userAnswers).value.value

          val result = AuthorisationsAndLimitDomain.limitReader(authorisationsDomain).apply(Nil).run(userAnswers)

          result.value.value must not be defined
          result.value.pages mustEqual Nil
        }

        "when additional declaration type is D" in {
          val authType = Gen.oneOf(authTypeACR, authTypeTRD, authTypeSSE).sample.value

          val userAnswers = emptyUserAnswers
            .setValue(AdditionalDeclarationTypePage, "D")
            .setValue(AuthorisationTypePage(authorisationIndex), authType)
            .setValue(AuthorisationReferenceNumberPage(authorisationIndex), authRefNumber)

          val authorisationsDomain = AuthorisationsDomain.userAnswersReader.apply(Nil).run(userAnswers).value.value

          val result = AuthorisationsAndLimitDomain.limitReader(authorisationsDomain).apply(Nil).run(userAnswers)

          result.value.value must not be defined
          result.value.pages mustEqual Nil
        }
      }

      "cannot be parsed from user answers" - {
        "when any AuthorisationType is ACR" - {
          "and additional declaration type is A" in {
            val userAnswers = emptyUserAnswers
              .setValue(AdditionalDeclarationTypePage, "A")
              .setValue(AuthorisationTypePage(authorisationIndex), authTypeACR)
              .setValue(AuthorisationReferenceNumberPage(authorisationIndex), authRefNumber)

            val authorisationsDomain = AuthorisationsDomain.userAnswersReader.apply(Nil).run(userAnswers).value.value

            val result = AuthorisationsAndLimitDomain.limitReader(authorisationsDomain).apply(Nil).run(userAnswers)

            result.left.value.page mustEqual LimitDatePage
            result.left.value.pages mustEqual Seq(
              LimitDatePage
            )
          }
        }
      }
    }
  }
}
