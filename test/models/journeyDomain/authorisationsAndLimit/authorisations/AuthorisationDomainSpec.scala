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
import config.Constants.DeclarationType._
import controllers.authorisationsAndLimit.authorisations.index.{routes => authorisationRoutes}
import controllers.authorisationsAndLimit.authorisations.{routes => authorisationsRoutes}
import forms.Constants.maxAuthorisationRefNumberLength
import generators.Generators
import models.ProcedureType.{Normal, Simplified}
import models.journeyDomain.Stage
import models.reference.InlandMode
import models.reference.authorisations.AuthorisationType
import models.{Index, Mode, ProcedureType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.authorisationsAndLimit.AddAuthorisationsYesNoPage
import pages.authorisationsAndLimit.authorisations.index.{AuthorisationReferenceNumberPage, AuthorisationTypePage, InferredAuthorisationTypePage}
import pages.external._
import pages.transportMeans.InlandModePage

class AuthorisationDomainSpec extends SpecBase with Generators {

  "AuthorisationDomain" - {

    "userAnswersReader" - {

      val authTypeACR = AuthorisationType("C521", "ACR - authorisation for the status of authorised consignor for Union transit")
      val authTypeTRD = AuthorisationType("C524", "TRD - authorisation to use transit declaration with a reduced dataset")

      val referenceNumber    = Gen.alphaNumStr.sample.value.take(maxAuthorisationRefNumberLength)
      val maritimeInlandMode = InlandMode("1", "Maritime")
      val railInlandMode     = InlandMode("2", "Rail")
      val airInlandMode      = InlandMode("4", "Air")
      val diffInlandMode     = arbitraryNonMaritimeRailAirInlandMode.arbitrary.sample.value

      val inlandModes                  = Seq(maritimeInlandMode, railInlandMode, airInlandMode, diffInlandMode)
      val authorisationTypeInlandModes = List(maritimeInlandMode, railInlandMode, airInlandMode)

      "can be parsed from UserAnswers" - {

        "when DeclarationType is TIR (reduced data set is 0)" in {
          val inlandModeGen = Gen.oneOf(inlandModes.diff(authorisationTypeInlandModes))

          forAll(inlandModeGen, arbitrary[AuthorisationType]) {
            (inlandMode, authorisationType) =>
              val userAnswers = emptyUserAnswers
                .setValue(ProcedureTypePage, Normal)
                .setValue(DeclarationTypePage, TIR)
                .setValue(InlandModePage, inlandMode)
                .setValue(AuthorisationTypePage(authorisationIndex), authorisationType)
                .setValue(AuthorisationReferenceNumberPage(authorisationIndex), referenceNumber)

              val expectedResult = AuthorisationDomain(
                authorisationType = authorisationType,
                referenceNumber = referenceNumber
              )(authorisationIndex)

              val result = AuthorisationDomain.userAnswersReader(authorisationIndex).apply(Nil).run(userAnswers)

              result.value.value mustBe expectedResult
              result.value.pages mustBe Seq(
                AuthorisationTypePage(authorisationIndex),
                AuthorisationReferenceNumberPage(authorisationIndex)
              )
          }
        }

        "when reduced data set indicator is 1" - {
          val declarationTypeGen = arbitrary[String](arbitraryNonTIRDeclarationType)

          "and inland mode is 1,2 or 4" in {
            val inlandModeGen = Gen.oneOf(authorisationTypeInlandModes)

            forAll(inlandModeGen, arbitrary[ProcedureType.Value], declarationTypeGen) {
              (inlandMode, procedureType, declarationType) =>
                val userAnswers = emptyUserAnswers
                  .setValue(ProcedureTypePage, procedureType)
                  .setValue(DeclarationTypePage, declarationType)
                  .setValue(ApprovedOperatorPage, true)
                  .setValue(InlandModePage, inlandMode)
                  .setValue(InferredAuthorisationTypePage(authorisationIndex), authTypeTRD)
                  .setValue(AuthorisationReferenceNumberPage(authorisationIndex), referenceNumber)

                val expectedResult = AuthorisationDomain(
                  authorisationType = authTypeTRD,
                  referenceNumber = referenceNumber
                )(authorisationIndex)

                val result = AuthorisationDomain.userAnswersReader(authorisationIndex).apply(Nil).run(userAnswers)

                result.value.value mustBe expectedResult
                result.value.pages mustBe Seq(
                  AuthorisationReferenceNumberPage(authorisationIndex)
                )
            }
          }

          "and inland mode is not 1,2 or 4" - {
            val inlandModeGen = Gen.oneOf(inlandModes.diff(authorisationTypeInlandModes))
            "and procedure type is simplified" in {

              forAll(inlandModeGen, declarationTypeGen) {
                (inlandMode, declarationType) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(ProcedureTypePage, Simplified)
                    .setValue(DeclarationTypePage, declarationType)
                    .setValue(ApprovedOperatorPage, true)
                    .setValue(InlandModePage, inlandMode)
                    .setValue(InferredAuthorisationTypePage(authorisationIndex), authTypeACR)
                    .setValue(AuthorisationReferenceNumberPage(authorisationIndex), referenceNumber)

                  val expectedResult = AuthorisationDomain(
                    authorisationType = authTypeACR,
                    referenceNumber = referenceNumber
                  )(authorisationIndex)

                  val result = AuthorisationDomain.userAnswersReader(authorisationIndex).apply(Nil).run(userAnswers)

                  result.value.value mustBe expectedResult
                  result.value.pages mustBe Seq(
                    AuthorisationReferenceNumberPage(authorisationIndex)
                  )
              }
            }

            "and procedure type is normal" in {
              forAll(inlandModeGen, arbitrary[AuthorisationType], declarationTypeGen) {
                (inlandMode, authorisationType, declarationType) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(ProcedureTypePage, Normal)
                    .setValue(DeclarationTypePage, declarationType)
                    .setValue(ApprovedOperatorPage, true)
                    .setValue(InlandModePage, inlandMode)
                    .setValue(AuthorisationTypePage(authorisationIndex), authorisationType)
                    .setValue(AuthorisationReferenceNumberPage(authorisationIndex), referenceNumber)

                  val expectedResult = AuthorisationDomain(
                    authorisationType = authorisationType,
                    referenceNumber = referenceNumber
                  )(authorisationIndex)

                  val result = AuthorisationDomain.userAnswersReader(authorisationIndex).apply(Nil).run(userAnswers)

                  result.value.value mustBe expectedResult
                  result.value.pages mustBe Seq(
                    AuthorisationTypePage(authorisationIndex),
                    AuthorisationReferenceNumberPage(authorisationIndex)
                  )
              }
            }
          }
        }

        "when reduced data set indicator is 0" in {

          forAll(arbitrary[ProcedureType.Value], arbitrary[InlandMode], arbitrary[AuthorisationType], arbitrary[String](arbitraryNonTIRDeclarationType)) {
            (procedureType, inlandMode, authorisationType, declarationType) =>
              val userAnswers = emptyUserAnswers
                .setValue(ProcedureTypePage, procedureType)
                .setValue(DeclarationTypePage, declarationType)
                .setValue(ApprovedOperatorPage, false)
                .setValue(InlandModePage, inlandMode)
                .setValue(AddAuthorisationsYesNoPage, true)
                .setValue(AuthorisationTypePage(authorisationIndex), authorisationType)
                .setValue(AuthorisationReferenceNumberPage(authorisationIndex), referenceNumber)

              val expectedResult = AuthorisationDomain(
                authorisationType = authorisationType,
                referenceNumber = referenceNumber
              )(authorisationIndex)

              val result = AuthorisationDomain.userAnswersReader(authorisationIndex).apply(Nil).run(userAnswers)

              result.value.value mustBe expectedResult
              result.value.pages mustBe Seq(
                AuthorisationTypePage(authorisationIndex),
                AuthorisationReferenceNumberPage(authorisationIndex)
              )
          }
        }
      }

      "cannot be parsed from user answers" - {

        "and reduced data set is 0" - {
          "must go to authorisation type page" in {
            forAll(arbitrary[ProcedureType.Value], arbitrary[InlandMode], arbitrary[String](arbitraryNonTIRDeclarationType)) {
              (procedureType, inlandMode, declarationType) =>
                val userAnswers = emptyUserAnswers
                  .setValue(ProcedureTypePage, procedureType)
                  .setValue(DeclarationTypePage, declarationType)
                  .setValue(ApprovedOperatorPage, false)
                  .setValue(InlandModePage, inlandMode)
                  .setValue(AddAuthorisationsYesNoPage, true)

                val result = AuthorisationDomain.userAnswersReader(authorisationIndex).apply(Nil).run(userAnswers)

                result.left.value.page mustBe AuthorisationTypePage(index)
                result.left.value.pages mustBe Seq(
                  AuthorisationTypePage(authorisationIndex)
                )
            }
          }
        }

        "and DeclarationType is TIR (reduced data set is 0)" - {
          "must go to authorisation type" in {
            val inlandModeGen = Gen.oneOf(authorisationTypeInlandModes)
            forAll(inlandModeGen) {
              inlandMode =>
                val userAnswers = emptyUserAnswers
                  .setValue(ProcedureTypePage, Normal)
                  .setValue(DeclarationTypePage, TIR)
                  .setValue(InlandModePage, inlandMode)

                val result = AuthorisationDomain.userAnswersReader(authorisationIndex).apply(Nil).run(userAnswers)

                result.left.value.page mustBe AuthorisationTypePage(index)
                result.left.value.pages mustBe Seq(
                  AuthorisationTypePage(authorisationIndex)
                )
            }
          }
        }

        "and reduced data set indicator is 1" - {
          val declarationTypeGen = arbitrary[String](arbitraryNonTIRDeclarationType)

          "and inland mode is 1,2 or 4 " - {
            "must bypass authorisation type and go to authorisation reference number" in {
              val inlandModeGen = Gen.oneOf(authorisationTypeInlandModes)
              forAll(inlandModeGen, declarationTypeGen) {
                (inlandMode, declarationType) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(ProcedureTypePage, Normal)
                    .setValue(DeclarationTypePage, declarationType)
                    .setValue(ApprovedOperatorPage, true)
                    .setValue(InlandModePage, inlandMode)
                    .setValue(InferredAuthorisationTypePage(authorisationIndex), authTypeTRD)

                  val result = AuthorisationDomain.userAnswersReader(authorisationIndex).apply(Nil).run(userAnswers)

                  result.left.value.page mustBe AuthorisationReferenceNumberPage(index)
                  result.left.value.pages mustBe Seq(
                    AuthorisationReferenceNumberPage(authorisationIndex)
                  )
              }
            }
          }

          "and inland mode is not 1,2 or 4" - {
            "and procedure type is simplified" - {
              "must bypass authorisation type and go to authorisation reference number" in {
                val inlandModeGen = Gen.oneOf(inlandModes.diff(authorisationTypeInlandModes))
                forAll(inlandModeGen, declarationTypeGen) {
                  (inlandMode, declarationType) =>
                    val userAnswers = emptyUserAnswers
                      .setValue(ProcedureTypePage, Simplified)
                      .setValue(DeclarationTypePage, declarationType)
                      .setValue(ApprovedOperatorPage, true)
                      .setValue(InlandModePage, inlandMode)
                      .setValue(InferredAuthorisationTypePage(authorisationIndex), authTypeACR)

                    val result = AuthorisationDomain.userAnswersReader(authorisationIndex).apply(Nil).run(userAnswers)

                    result.left.value.page mustBe AuthorisationReferenceNumberPage(index)
                    result.left.value.pages mustBe Seq(
                      AuthorisationReferenceNumberPage(authorisationIndex)
                    )
                }
              }
            }
          }
        }
      }
    }

    "routeIfCompleted" - {
      "when accessing journey" - {
        "must be auth. ref. number page" in {
          forAll(arbitrary[AuthorisationType], Gen.alphaNumStr, arbitrary[Mode], arbitrary[Index]) {
            (authorisationType, referenceNumber, mode, index) =>
              val authorisation = AuthorisationDomain(authorisationType, referenceNumber)(index)

              authorisation.routeIfCompleted(emptyUserAnswers, mode, Stage.AccessingJourney).value mustBe
                authorisationRoutes.AuthorisationReferenceNumberController.onPageLoad(lrn, mode, index)
          }
        }
      }

      "when completing journey" - {
        "and auth type inferred at next index but auth number isn't" in {
          forAll(arbitrary[AuthorisationType], Gen.alphaNumStr, arbitrary[Mode]) {
            (authorisationType, referenceNumber, mode) =>
              val authorisation = AuthorisationDomain(authorisationType, referenceNumber)(Index(0))
              val userAnswers = emptyUserAnswers
                .setValue(InferredAuthorisationTypePage(Index(0)), authorisationType)
                .setValue(AuthorisationReferenceNumberPage(Index(0)), referenceNumber)
                .setValue(InferredAuthorisationTypePage(Index(1)), authorisationType)

              authorisation.routeIfCompleted(userAnswers, mode, Stage.CompletingJourney).value mustBe
                authorisationRoutes.AuthorisationReferenceNumberController.onPageLoad(lrn, mode, Index(1))
          }
        }

        "and nothing at next index" in {
          forAll(arbitrary[AuthorisationType], Gen.alphaNumStr, arbitrary[Mode]) {
            (authorisationType, referenceNumber, mode) =>
              val authorisation = AuthorisationDomain(authorisationType, referenceNumber)(Index(0))

              authorisation.routeIfCompleted(emptyUserAnswers, mode, Stage.CompletingJourney).value mustBe
                authorisationsRoutes.AddAnotherAuthorisationController.onPageLoad(lrn, mode)
          }
        }
      }
    }
  }
}
