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
import models.authorisations.AuthorisationType
import models.transportMeans.departure.InlandMode
import models.transportMeans.{active, BorderModeOfTransport}
import models.{DeclarationType, Index, ProcedureType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.{ApprovedOperatorPage, DeclarationTypePage, ProcedureTypePage}
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.departure.InlandModePage

class InferenceServiceSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val service = injector.instanceOf[InferenceService]

  "inferActiveIdentifier" - {
    "when first index" - {
      val index = Index(0)
      "when border mode is ChannelTunnel" - {
        "must infer answer as TrainNumber" in {
          val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, BorderModeOfTransport.ChannelTunnel)
          service.inferActiveIdentifier(userAnswers, index) mustBe Some(active.Identification.TrainNumber)
        }
      }

      "when border mode is IrishLandBoundary" - {
        "must infer answer as RegNumberRoadVehicle" in {
          val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, BorderModeOfTransport.IrishLandBoundary)
          service.inferActiveIdentifier(userAnswers, index) mustBe Some(active.Identification.RegNumberRoadVehicle)
        }
      }

      "when border mode is something else" - {
        "must not infer answer" in {
          val borderModeGen = Gen
            .oneOf(BorderModeOfTransport.values)
            .filterNot(_ == BorderModeOfTransport.ChannelTunnel)
            .filterNot(_ == BorderModeOfTransport.IrishLandBoundary)

          forAll(borderModeGen) {
            borderMode =>
              val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, borderMode)
              service.inferActiveIdentifier(userAnswers, index) mustBe None
          }
        }
      }
    }

    "when not first index" - {
      val index = Index(1)
      "must not infer answer" in {
        forAll(arbitrary[BorderModeOfTransport]) {
          borderMode =>
            val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, borderMode)
            service.inferActiveIdentifier(userAnswers, index) mustBe None
        }
      }
    }
  }

  "inferAuthorisationType" - {
    "when first index" - {
      val index = Index(0)
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

              service.inferAuthorisationType(userAnswers, index) mustBe Some(AuthorisationType.TRD)
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

              service.inferAuthorisationType(userAnswers, index) mustBe Some(AuthorisationType.ACR)
          }
        }
      }

      "when TIR" - {
        "must not infer answer" in {
          forAll(arbitrary[ProcedureType], arbitrary[InlandMode]) {
            (procedureType, inlandMode) =>
              val userAnswers = emptyUserAnswers
                .setValue(ProcedureTypePage, procedureType)
                .setValue(DeclarationTypePage, DeclarationType.Option4)
                .setValue(InlandModePage, inlandMode)

              service.inferAuthorisationType(userAnswers, index) mustBe None
          }
        }
      }

      "when not using a reduced data set" - {
        "must not infer answer" in {
          val declarationTypeGen = arbitrary[DeclarationType](arbitraryNonOption4DeclarationType)

          forAll(arbitrary[ProcedureType], declarationTypeGen, arbitrary[InlandMode]) {
            (procedureType, declarationType, inlandMode) =>
              val userAnswers = emptyUserAnswers
                .setValue(ProcedureTypePage, procedureType)
                .setValue(DeclarationTypePage, declarationType)
                .setValue(ApprovedOperatorPage, false)
                .setValue(InlandModePage, inlandMode)

              service.inferAuthorisationType(userAnswers, index) mustBe None
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

            service.inferAuthorisationType(userAnswers, index) mustBe None
        }
      }
    }
  }

}
