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

package models.authorisations

import base.SpecBase
import generators.Generators
import models.Index
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.authorisationsAndLimit.authorisations.index.{AuthorisationTypePage, InferredAuthorisationTypePage}
import play.api.libs.json.{JsError, JsString, Json}

class AuthorisationTypeSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "AuthorisationType" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(AuthorisationType.values)

      forAll(gen) {
        AuthorisationType =>
          JsString(AuthorisationType.toString).validate[AuthorisationType].asOpt.value mustEqual AuthorisationType
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!AuthorisationType.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[AuthorisationType] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(AuthorisationType.values)

      forAll(gen) {
        AuthorisationType =>
          Json.toJson(AuthorisationType) mustEqual JsString(AuthorisationType.toString)
      }
    }

    "values" - {
      "when no auth types in user answers" - {
        "must return all values" in {
          val result = AuthorisationType.values(emptyUserAnswers, Index(0))

          result mustBe Seq(
            AuthorisationType.ACR,
            AuthorisationType.SSE,
            AuthorisationType.TRD
          )
        }
      }

      "when there is an auth type in user answers" - {
        "must filter out that type" - {
          "when it has been inferred" in {
            val userAnswers = emptyUserAnswers
              .setValue(InferredAuthorisationTypePage(Index(0)), AuthorisationType.ACR)

            val result = AuthorisationType.values(userAnswers, Index(1))

            result mustBe Seq(
              AuthorisationType.SSE,
              AuthorisationType.TRD
            )
          }

          "when it has not been inferred" in {
            val userAnswers = emptyUserAnswers
              .setValue(AuthorisationTypePage(Index(0)), AuthorisationType.ACR)

            val result = AuthorisationType.values(userAnswers, Index(1))

            result mustBe Seq(
              AuthorisationType.SSE,
              AuthorisationType.TRD
            )
          }
        }
      }

      "when all 3 auth types are in user answers" - {
        "must return empty list" in {
          val userAnswers = emptyUserAnswers
            .setValue(InferredAuthorisationTypePage(Index(0)), AuthorisationType.ACR)
            .setValue(AuthorisationTypePage(Index(1)), AuthorisationType.SSE)
            .setValue(InferredAuthorisationTypePage(Index(2)), AuthorisationType.TRD)

          val result = AuthorisationType.values(userAnswers, Index(3))

          result mustBe Seq.empty
        }
      }

      "when one auth type has been added and we are that index" - {
        "must return all 3 auth types" in {
          val userAnswers = emptyUserAnswers
            .setValue(InferredAuthorisationTypePage(Index(0)), AuthorisationType.ACR)

          val result = AuthorisationType.values(userAnswers, Index(0))

          result mustBe Seq(
            AuthorisationType.ACR,
            AuthorisationType.SSE,
            AuthorisationType.TRD
          )
        }
      }
    }
  }
}
