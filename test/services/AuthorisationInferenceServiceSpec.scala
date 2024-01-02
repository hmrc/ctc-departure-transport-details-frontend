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
import generators.Generators
import models.Index
import models.ProcedureType.{Normal, Simplified}
import models.reference.InlandMode
import models.reference.authorisations.AuthorisationType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.authorisationsAndLimit.authorisations.index.InferredAuthorisationTypePage
import pages.external.{ApprovedOperatorPage, DeclarationTypePage, ProcedureTypePage}
import pages.transportMeans.InlandModePage

class AuthorisationInferenceServiceSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val declarationTypeGen: Gen[String] = arbitrary[String](arbitraryNonTIRDeclarationType)

  val authTypeACR: AuthorisationType = AuthorisationType(
    "C521",
    "ACR - authorisation for the status of authorised consignor for Union transit"
  )

  val authTypeTRD: AuthorisationType = AuthorisationType(
    "C524",
    "TRD - authorisation to use transit declaration with a reduced dataset"
  )

  val authTypes: Seq[AuthorisationType] = Seq(authTypeACR, authTypeTRD)

  "inferAuthorisations" - {

    "when reduced dataset indicator is 1 and inland mode is Maritime/Rail/Air and ProcedureType is Normal" - {
      "must infer index 0 as TRD Authorisation Type" in {
        forAll(arbitrary[InlandMode](arbitraryMaritimeRailAirInlandMode), declarationTypeGen) {
          (inlandMode, declarationType) =>
            val userAnswers = emptyUserAnswers
              .setValue(InlandModePage, inlandMode)
              .setValue(DeclarationTypePage, declarationType)
              .setValue(ApprovedOperatorPage, true)
              .setValue(ProcedureTypePage, Normal)

            val service = new AuthorisationInferenceService()

            val result = service.inferAuthorisations(userAnswers, authTypes)

            val expectedResult = userAnswers
              .setValue(InferredAuthorisationTypePage(Index(0)), authTypeTRD)

            result mustBe expectedResult
        }
      }

      "must not infer index 0 as TRD when TRD isn't present in reference data" in {
        forAll(arbitrary[InlandMode](arbitraryMaritimeRailAirInlandMode), declarationTypeGen) {
          (inlandMode, declarationType) =>
            val userAnswers = emptyUserAnswers
              .setValue(InlandModePage, inlandMode)
              .setValue(DeclarationTypePage, declarationType)
              .setValue(ApprovedOperatorPage, true)
              .setValue(ProcedureTypePage, Normal)

            val service = new AuthorisationInferenceService()

            val result = service.inferAuthorisations(userAnswers, Seq.empty)

            result.get(InferredAuthorisationTypePage(Index(0))) must not be defined
        }
      }
    }

    "when ProcedureType is Simplified" - {
      "must infer index 0 as ACR Authorisation Type" in {
        forAll(arbitrary[Option[InlandMode]], declarationTypeGen) {
          (inlandMode, declarationType) =>
            val userAnswers = emptyUserAnswers
              .setValue(InlandModePage, inlandMode)
              .setValue(DeclarationTypePage, declarationType)
              .setValue(ApprovedOperatorPage, true)
              .setValue(ProcedureTypePage, Simplified)

            val service = new AuthorisationInferenceService()

            val result = service.inferAuthorisations(userAnswers, authTypes)

            val expectedResult = userAnswers
              .setValue(InferredAuthorisationTypePage(Index(0)), authTypeACR)

            result mustBe expectedResult
        }
      }

      "must not infer index 0 as ACR when they are not present in reference data" in {
        forAll(arbitrary[InlandMode], declarationTypeGen) {
          (inlandMode, declarationType) =>
            val userAnswers = emptyUserAnswers
              .setValue(InlandModePage, inlandMode)
              .setValue(DeclarationTypePage, declarationType)
              .setValue(ApprovedOperatorPage, true)
              .setValue(ProcedureTypePage, Simplified)

            val service = new AuthorisationInferenceService()

            val result = service.inferAuthorisations(userAnswers, Seq.empty)

            result.get(InferredAuthorisationTypePage(Index(0))) must not be defined
        }
      }
    }

    "when reduced dataset indicator is 0 and inland mode is Maritime/Rail/Air and ProcedureType is Normal" - {
      "must not make any inferences" in {
        forAll(arbitrary[InlandMode](arbitraryMaritimeRailAirInlandMode), declarationTypeGen) {
          (inlandMode, declarationType) =>
            val userAnswers = emptyUserAnswers
              .setValue(InlandModePage, inlandMode)
              .setValue(DeclarationTypePage, declarationType)
              .setValue(ApprovedOperatorPage, false)
              .setValue(ProcedureTypePage, Normal)

            val service = new AuthorisationInferenceService()

            val result = service.inferAuthorisations(userAnswers, authTypes)

            result mustBe userAnswers
        }
      }
    }

    "when reduced dataset indicator is 1 and inland mode is not Maritime/Rail/Air and ProcedureType is Normal" - {
      "must not make any inferences" in {
        forAll(arbitrary[Option[InlandMode]](arbitraryOptionalNonMaritimeRailAirInlandMode), declarationTypeGen) {
          (inlandMode, declarationType) =>
            val userAnswers = emptyUserAnswers
              .setValue(InlandModePage, inlandMode)
              .setValue(DeclarationTypePage, declarationType)
              .setValue(ApprovedOperatorPage, true)
              .setValue(ProcedureTypePage, Normal)

            val service = new AuthorisationInferenceService()

            val result = service.inferAuthorisations(userAnswers, authTypes)

            result mustBe userAnswers
        }
      }
    }

    "when reduced dataset indicator is 0 and inland mode is not Maritime/Rail/Air and ProcedureType is Normal" - {
      "must not make any inferences" in {
        forAll(arbitrary[Option[InlandMode]](arbitraryOptionalNonMaritimeRailAirInlandMode), declarationTypeGen) {
          (inlandMode, declarationType) =>
            val userAnswers = emptyUserAnswers
              .setValue(InlandModePage, inlandMode)
              .setValue(DeclarationTypePage, declarationType)
              .setValue(ApprovedOperatorPage, false)
              .setValue(ProcedureTypePage, Normal)

            val service = new AuthorisationInferenceService()

            val result = service.inferAuthorisations(userAnswers, authTypes)

            result mustBe userAnswers
        }
      }
    }
  }
}
