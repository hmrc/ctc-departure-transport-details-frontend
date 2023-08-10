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
import models.ProcedureType.Simplified
import models.transportMeans.InlandMode
import models.{DeclarationType, Index, ProcedureType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.{ApprovedOperatorPage, DeclarationTypePage, ProcedureTypePage}
import pages.transportMeans.InlandModePage
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
      "when first index" - {
        val index = Index(0)
        "when not a reduced data set, is simplified and is Maritime/Rail/Air inland mode" - {
          "must infer answers as ACR" in {
            val declarationTypeGen = arbitrary[DeclarationType](arbitraryNonOption4DeclarationType)

            val inlandModeGen = Gen.oneOf(
              InlandMode.Maritime,
              InlandMode.Rail,
              InlandMode.Air
            )

            forAll(declarationTypeGen, inlandModeGen) {
              (declarationType, inlandMode) =>
                val userAnswers = emptyUserAnswers
                  .setValue(ProcedureTypePage, Simplified)
                  .setValue(DeclarationTypePage, declarationType)
                  .setValue(ApprovedOperatorPage, false)
                  .setValue(InlandModePage, inlandMode)

                AuthorisationType.values(userAnswers, index) mustBe Seq(AuthorisationType.ACR)
            }
          }
        }

        "when reduced data set, maritime/rail/air inland mode" - {
          "must infer answer as TRD" in {
            val declarationTypeGen = arbitrary[DeclarationType](arbitraryNonOption4DeclarationType)

            val inlandModeGen = Gen.oneOf(
              InlandMode.Maritime,
              InlandMode.Rail,
              InlandMode.Air
            )

            forAll(arbitrary[ProcedureType], declarationTypeGen, inlandModeGen) {
              (procedureType, declarationType, inlandMode) =>
                val userAnswers = emptyUserAnswers
                  .setValue(ProcedureTypePage, procedureType)
                  .setValue(DeclarationTypePage, declarationType)
                  .setValue(ApprovedOperatorPage, true)
                  .setValue(InlandModePage, inlandMode)

                AuthorisationType.values(userAnswers, index) mustBe Seq(AuthorisationType.TRD)
            }
          }
        }

        "when reduced data set, road/mail/fixed/waterway inland mode, simplified procedure type" - {
          "must infer answer as ACR" in {
            val declarationTypeGen = arbitrary[DeclarationType](arbitraryNonOption4DeclarationType)

            val inlandModeGen = Gen.oneOf(
              InlandMode.Road,
              InlandMode.Mail,
              InlandMode.Fixed,
              InlandMode.Waterway
            )

            forAll(declarationTypeGen, inlandModeGen) {
              (declarationType, inlandMode) =>
                val userAnswers = emptyUserAnswers
                  .setValue(ProcedureTypePage, ProcedureType.Simplified)
                  .setValue(DeclarationTypePage, declarationType)
                  .setValue(ApprovedOperatorPage, true)
                  .setValue(InlandModePage, inlandMode)

                AuthorisationType.values(userAnswers, index) mustBe Seq(AuthorisationType.ACR)
            }
          }
        }

        "when TIR" - {
          "must not infer answer if inlandMode is Road/Fixed/Mail/Waterway" in {

            val inlandModeGen = Gen.oneOf(
              InlandMode.Road,
              InlandMode.Mail,
              InlandMode.Fixed,
              InlandMode.Waterway
            )

            forAll(arbitrary[ProcedureType], inlandModeGen) {
              (procedureType, inlandMode) =>
                val userAnswers = emptyUserAnswers
                  .setValue(ProcedureTypePage, procedureType)
                  .setValue(DeclarationTypePage, DeclarationType.Option4)
                  .setValue(InlandModePage, inlandMode)

                AuthorisationType.values(userAnswers, index) mustBe AuthorisationType.values
            }
          }

          "must infer answer if inlandMode is Maritime/Air/Rail and procedure type is simplified" in {

            val inlandModeGen = Gen.oneOf(
              InlandMode.Maritime,
              InlandMode.Air,
              InlandMode.Rail
            )

            forAll(inlandModeGen) {
              inlandMode =>
                val userAnswers = emptyUserAnswers
                  .setValue(ProcedureTypePage, Simplified)
                  .setValue(DeclarationTypePage, DeclarationType.Option4)
                  .setValue(InlandModePage, inlandMode)

                AuthorisationType.values(userAnswers, index) mustBe Seq(AuthorisationType.ACR)
            }
          }
        }

        "when not using a reduced data set" - {
          "must not infer answer if inlandMode is Road/Fixed/Mail/Waterway" in {
            val declarationTypeGen = arbitrary[DeclarationType](arbitraryNonOption4DeclarationType)

            val inlandModeGen = Gen.oneOf(
              InlandMode.Road,
              InlandMode.Mail,
              InlandMode.Fixed,
              InlandMode.Waterway
            )

            forAll(arbitrary[ProcedureType], declarationTypeGen, inlandModeGen) {
              (procedureType, declarationType, inlandMode) =>
                val userAnswers = emptyUserAnswers
                  .setValue(ProcedureTypePage, procedureType)
                  .setValue(DeclarationTypePage, declarationType)
                  .setValue(ApprovedOperatorPage, false)
                  .setValue(InlandModePage, inlandMode)

                AuthorisationType.values(userAnswers, index) mustBe AuthorisationType.values
            }
          }
        }
      }

      "when not first index" - {
        val index = Index(1)
        "must not infer answer" in {
          forAll(arbitrary[ProcedureType], arbitrary[DeclarationType], arbitrary[Boolean], arbitrary[InlandMode]) {
            (procedureType, declarationType, approvedOperator, inlandMode) =>
              val userAnswers = emptyUserAnswers
                .setValue(ProcedureTypePage, procedureType)
                .setValue(DeclarationTypePage, declarationType)
                .setValue(ApprovedOperatorPage, approvedOperator)
                .setValue(InlandModePage, inlandMode)

              AuthorisationType.values(userAnswers, index) mustBe AuthorisationType.values
          }
        }
      }
    }
  }
}
