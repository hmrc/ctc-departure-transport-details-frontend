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

package models.journeyDomain

import base.SpecBase
import generators.Generators
import models.DeclarationType
import models.ProcedureType.{Normal, Simplified}
import models.domain.{EitherType, UserAnswersReader}
import models.journeyDomain.authorisationsAndLimit.authorisations.AuthorisationsAndLimitDomain
import models.transportMeans.InlandMode
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.authorisationsAndLimit.{AddAuthorisationsYesNoPage, AuthorisationsInferredPage}
import pages.carrierDetails.CarrierDetailYesNoPage
import pages.external.{ApprovedOperatorPage, DeclarationTypePage, ProcedureTypePage}
import pages.supplyChainActors.SupplyChainActorYesNoPage
import pages.transportMeans.{AddInlandModeYesNoPage, InlandModePage}

class TransportDomainSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "can be parsed from user answers" - {

    "when addInlandMode is answered yes" - {
      "when Mail inland mode" in {
        val initialUserAnswers = emptyUserAnswers
          .setValue(AddInlandModeYesNoPage, true)
          .setValue(InlandModePage, InlandMode.Mail)
        forAll(arbitraryTransportAnswers(initialUserAnswers)) {
          userAnswers =>
            val result: EitherType[TransportDomain] = UserAnswersReader[TransportDomain].run(userAnswers)
            result.value.transportMeans must not be defined
        }
      }

      "when non-Mail inland mode" in {
        forAll(arbitrary[InlandMode](arbitraryNonMailInlandMode)) {
          inlandMode =>
            val initialUserAnswers = emptyUserAnswers
              .setValue(AddInlandModeYesNoPage, true)
              .setValue(InlandModePage, inlandMode)
            forAll(arbitraryTransportAnswers(initialUserAnswers)) {
              userAnswers =>
                val result: EitherType[TransportDomain] = UserAnswersReader[TransportDomain].run(userAnswers)
                result.value.transportMeans must be(defined)
            }
        }
      }
    }

    "when addInlandMode is answered no" in {

      val initialUserAnswers = emptyUserAnswers.setValue(AddInlandModeYesNoPage, false)
      forAll(arbitraryTransportAnswers(initialUserAnswers)) {
        userAnswers =>
          val result: EitherType[TransportDomain] = UserAnswersReader[TransportDomain].run(userAnswers)
          result.value.transportMeans must be(defined)
      }
    }

    "when reduced data set indicator is true" in {
      forAll(arbitrary[DeclarationType](arbitraryNonOption4DeclarationType)) {
        declarationType =>
          val initialUserAnswers = emptyUserAnswers
            .setValue(DeclarationTypePage, declarationType)
            .setValue(ApprovedOperatorPage, true)

          forAll(arbitraryTransportAnswers(initialUserAnswers)) {
            userAnswers =>
              val result: EitherType[TransportDomain] = UserAnswersReader[TransportDomain].run(userAnswers)
              result.value.authorisationsAndLimit must be(defined)
          }
      }
    }

    "when procedure type is Normal and reduced data set indicator is undefined" - {
      "and not adding authorisations" in {
        val initialUserAnswers = emptyUserAnswers
          .setValue(DeclarationTypePage, DeclarationType.Option4)
          .setValue(ProcedureTypePage, Normal)
          .setValue(AddAuthorisationsYesNoPage, false)

        forAll(arbitraryTransportAnswers(initialUserAnswers)) {
          userAnswers =>
            val result: EitherType[TransportDomain] = UserAnswersReader[TransportDomain].run(userAnswers)
            result.value.authorisationsAndLimit must not be defined
        }
      }

      "and adding authorisations" in {
        val initialUserAnswers = emptyUserAnswers
          .setValue(DeclarationTypePage, DeclarationType.Option4)
          .setValue(AddAuthorisationsYesNoPage, true)

        forAll(arbitraryTransportAnswers(initialUserAnswers)) {
          userAnswers =>
            val result: EitherType[TransportDomain] = UserAnswersReader[TransportDomain].run(userAnswers)
            result.value.authorisationsAndLimit must be(defined)
        }
      }
    }

    "when reduced data set indicator is false and procedure type is Normal" - {
      "and not adding authorisations" in {
        forAll(arbitrary[DeclarationType](arbitraryNonOption4DeclarationType)) {
          declarationType =>
            val initialUserAnswers = emptyUserAnswers
              .setValue(DeclarationTypePage, declarationType)
              .setValue(ApprovedOperatorPage, false)
              .setValue(ProcedureTypePage, Normal)
              .setValue(AddAuthorisationsYesNoPage, false)

            forAll(arbitraryTransportAnswers(initialUserAnswers)) {
              userAnswers =>
                val result: EitherType[TransportDomain] = UserAnswersReader[TransportDomain].run(userAnswers)
                result.value.authorisationsAndLimit must not be defined
            }
        }
      }

      "and adding authorisations" in {
        forAll(arbitrary[DeclarationType](arbitraryNonOption4DeclarationType)) {
          declarationType =>
            val initialUserAnswers = emptyUserAnswers
              .setValue(DeclarationTypePage, declarationType)
              .setValue(ApprovedOperatorPage, false)
              .setValue(ProcedureTypePage, Normal)
              .setValue(AddAuthorisationsYesNoPage, true)

            forAll(arbitraryTransportAnswers(initialUserAnswers)) {
              userAnswers =>
                val result: EitherType[TransportDomain] = UserAnswersReader[TransportDomain].run(userAnswers)
                result.value.authorisationsAndLimit must be(defined)
            }
        }
      }
    }

    "when reduced data set indicator is false and procedure type is Simplified" - {
      forAll(arbitrary[DeclarationType](arbitraryNonOption4DeclarationType)) {
        declarationType =>
          val initialUserAnswers = emptyUserAnswers
            .setValue(DeclarationTypePage, declarationType)
            .setValue(ApprovedOperatorPage, false)
            .setValue(ProcedureTypePage, Simplified)

          forAll(arbitraryTransportAnswers(initialUserAnswers)) {
            userAnswers =>
              val result: EitherType[TransportDomain] = UserAnswersReader[TransportDomain].run(userAnswers)
              result.value.authorisationsAndLimit must be(defined)
          }
      }
    }

    "when reduced data set indicator is true and procedure type is Normal" in {
      forAll(arbitrary[DeclarationType](arbitraryNonOption4DeclarationType)) {
        declarationType =>
          val initialUserAnswers = emptyUserAnswers
            .setValue(DeclarationTypePage, declarationType)
            .setValue(ApprovedOperatorPage, true)
            .setValue(ProcedureTypePage, Normal)

          forAll(arbitraryTransportAnswers(initialUserAnswers)) {
            userAnswers =>
              val result: EitherType[TransportDomain] = UserAnswersReader[TransportDomain].run(userAnswers)
              result.value.authorisationsAndLimit must be(defined)
          }
      }
    }

    "when adding supply chain actors" in {
      val initialUserAnswers = emptyUserAnswers.setValue(SupplyChainActorYesNoPage, true)
      forAll(arbitraryTransportAnswers(initialUserAnswers)) {
        userAnswers =>
          val result: EitherType[TransportDomain] = UserAnswersReader[TransportDomain].run(userAnswers)
          result.value.supplyChainActors must be(defined)
      }
    }

    "when not adding supply chain actors" in {
      val initialUserAnswers = emptyUserAnswers.setValue(SupplyChainActorYesNoPage, false)
      forAll(arbitraryTransportAnswers(initialUserAnswers)) {
        userAnswers =>
          val result: EitherType[TransportDomain] = UserAnswersReader[TransportDomain].run(userAnswers)
          result.value.supplyChainActors must not be defined
      }
    }

    "when adding carrier details" in {
      val initialUserAnswers = emptyUserAnswers.setValue(CarrierDetailYesNoPage, true)
      forAll(arbitraryTransportAnswers(initialUserAnswers)) {
        userAnswers =>
          val result: EitherType[TransportDomain] = UserAnswersReader[TransportDomain].run(userAnswers)
          result.value.carrierDetails must be(defined)
      }
    }

    "when not adding carrier details" in {
      val initialUserAnswers = emptyUserAnswers.setValue(CarrierDetailYesNoPage, false)
      forAll(arbitraryTransportAnswers(initialUserAnswers)) {
        userAnswers =>
          val result: EitherType[TransportDomain] = UserAnswersReader[TransportDomain].run(userAnswers)
          result.value.carrierDetails must not be defined
      }
    }

    "authorisationsAndLimitReads" - {
      "can not be parsed from user answers" - {
        "when inference is not flagged as true" in {
          forAll(arbitrary[DeclarationType](arbitraryNonOption4DeclarationType)) {
            declarationType =>
              val userAnswers = emptyUserAnswers
                .setValue(DeclarationTypePage, declarationType)
                .setValue(ApprovedOperatorPage, true)
                .setValue(ProcedureTypePage, Normal)

              val result: EitherType[Option[AuthorisationsAndLimitDomain]] = TransportDomain.authorisationsAndLimitReads.run(userAnswers)
              result.left.value.page mustBe AuthorisationsInferredPage
          }
        }
      }
    }
  }
}
