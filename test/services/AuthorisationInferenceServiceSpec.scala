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

package services

import base.SpecBase
import cats.data.NonEmptySet
import generators.Generators
import models.ProcedureType.{Normal, Simplified}
import models.reference.authorisations.AuthorisationType
import models.{Index, ProcedureType, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.authorisationsAndLimit.authorisations.index.{InferredAuthorisationTypePage, IsMandatoryPage}
import pages.external.{ApprovedOperatorPage, DeclarationTypePage, ProcedureTypePage}

class AuthorisationInferenceServiceSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val declarationTypeGen: Gen[String] = arbitrary[String](arbitraryNonTIRDeclarationType)

  private val authTypeACR: AuthorisationType = AuthorisationType(
    "C521",
    "ACR"
  )

  private val authTypeSSE: AuthorisationType = AuthorisationType(
    "C523",
    "SSE"
  )

  private val authTypeTRD: AuthorisationType = AuthorisationType(
    "C524",
    "TRD"
  )

  private val authTypes: NonEmptySet[AuthorisationType] = NonEmptySet.of(authTypeACR, authTypeSSE, authTypeTRD)

  private def userAnswersGen(reducedDatasetIndicator: Boolean, procedureType: ProcedureType.Value): Gen[UserAnswers] =
    declarationTypeGen.map {
      declarationType =>
        emptyUserAnswers
          .setValue(DeclarationTypePage, declarationType)
          .setValue(ApprovedOperatorPage, reducedDatasetIndicator)
          .setValue(ProcedureTypePage, procedureType)
    }

  "inferAuthorisations" - {

    "when reduced dataset indicator is 1" - {
      val reducedDatasetIndicator = true

      "and ProcedureType is Normal" - {
        val procedureType = Normal

        "and authorisation types present in reference data" - {
          "must not infer anything" in {
            forAll(userAnswersGen(reducedDatasetIndicator, procedureType)) {
              userAnswers =>
                val service = new AuthorisationInferenceService()

                val result = service.inferAuthorisations(userAnswers, authTypes)

                result mustBe userAnswers
            }
          }
        }

        "and authorisation type not present in reference data" - {
          "must not infer anything" in {
            forAll(userAnswersGen(reducedDatasetIndicator, procedureType)) {
              userAnswers =>
                val service = new AuthorisationInferenceService()

                val result = service.inferAuthorisations(userAnswers, NonEmptySet.of(authTypeACR, authTypeSSE))

                result mustBe userAnswers
            }
          }
        }
      }

      "and ProcedureType is Simplified" - {
        val procedureType = Simplified

        "and authorisation types present in reference data" - {
          "must infer index 0 as ACR and index 1 as TRD" in {
            forAll(userAnswersGen(reducedDatasetIndicator, procedureType)) {
              userAnswers =>
                val service = new AuthorisationInferenceService()

                val result = service.inferAuthorisations(userAnswers, authTypes)

                val expectedResult = userAnswers
                  .setValue(InferredAuthorisationTypePage(Index(0)), authTypeACR)
                  .setValue(IsMandatoryPage(Index(0)), true)
                  .setValue(InferredAuthorisationTypePage(Index(1)), authTypeTRD)
                  .setValue(IsMandatoryPage(Index(1)), true)

                result mustBe expectedResult
            }
          }
        }

        "and authorisation type not present in reference data" - {
          "must not infer anything" in {
            forAll(userAnswersGen(reducedDatasetIndicator, procedureType)) {
              userAnswers =>
                val service = new AuthorisationInferenceService()

                val result = service.inferAuthorisations(userAnswers, NonEmptySet.of(authTypeSSE))

                result mustBe userAnswers
            }
          }
        }
      }
    }

    "when reduced dataset indicator is 0" - {
      val reducedDatasetIndicator = false

      "and ProcedureType is Normal" - {
        val procedureType = Normal

        "must not infer anything" in {
          forAll(userAnswersGen(reducedDatasetIndicator, procedureType)) {
            userAnswers =>
              val service = new AuthorisationInferenceService()

              val result = service.inferAuthorisations(userAnswers, authTypes)

              result mustBe userAnswers
          }
        }
      }

      "and ProcedureType is Simplified" - {
        val procedureType = Simplified

        "and authorisation type present in reference data" - {
          "must infer index 0 as ACR" in {
            forAll(userAnswersGen(reducedDatasetIndicator, procedureType)) {
              userAnswers =>
                val service = new AuthorisationInferenceService()

                val result = service.inferAuthorisations(userAnswers, authTypes)

                val expectedResult = userAnswers
                  .setValue(InferredAuthorisationTypePage(Index(0)), authTypeACR)
                  .setValue(IsMandatoryPage(Index(0)), true)

                result mustBe expectedResult
            }
          }
        }

        "and authorisation type not present in reference data" - {
          "must not infer anything" in {
            forAll(userAnswersGen(reducedDatasetIndicator, procedureType)) {
              userAnswers =>
                val service = new AuthorisationInferenceService()

                val result = service.inferAuthorisations(userAnswers, NonEmptySet.of(authTypeSSE, authTypeTRD))

                result mustBe userAnswers
            }
          }
        }
      }
    }
  }
}
