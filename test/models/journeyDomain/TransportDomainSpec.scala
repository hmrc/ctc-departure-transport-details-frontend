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
import config.Constants.DeclarationType.TIR
import generators.Generators
import models.ProcedureType.{Normal, Simplified}
import models.reference.InlandMode
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalInformation.AddAdditionalInformationYesNoPage
import pages.additionalReference.AddAdditionalReferenceYesNoPage
import pages.authorisationsAndLimit.{AddAuthorisationsYesNoPage, AuthorisationsInferredPage}
import pages.carrierDetails.CarrierDetailYesNoPage
import pages.external.{ApprovedOperatorPage, DeclarationTypePage, ProcedureTypePage}
import pages.sections.TransportSection
import pages.supplyChainActors.SupplyChainActorYesNoPage
import pages.transportMeans.{AddInlandModeYesNoPage, InlandModePage}

class TransportDomainSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "TransportDomain" - {

    val mailInlandMode = InlandMode("5", "Mail (Active mode of transport unknown)")

    "can be parsed from user answers" - {

      "when addInlandMode is answered yes" - {
        "when Mail inland mode" in {
          val initialUserAnswers = emptyUserAnswers
            .setValue(AddInlandModeYesNoPage, true)
            .setValue(InlandModePage, mailInlandMode)
          forAll(arbitraryTransportAnswers(initialUserAnswers)) {
            userAnswers =>
              val result = TransportDomain.userAnswersReader.run(userAnswers)
              result.value.value.transportMeans must not be defined
              result.value.pages.last mustEqual TransportSection
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
                  val result = TransportDomain.userAnswersReader.run(userAnswers)
                  result.value.value.transportMeans must be(defined)
                  result.value.pages.last mustEqual TransportSection
              }
          }
        }
      }

      "when addInlandMode is answered no" in {

        val initialUserAnswers = emptyUserAnswers.setValue(AddInlandModeYesNoPage, false)
        forAll(arbitraryTransportAnswers(initialUserAnswers)) {
          userAnswers =>
            val result = TransportDomain.userAnswersReader.run(userAnswers)
            result.value.value.transportMeans must be(defined)
            result.value.pages.last mustEqual TransportSection
        }
      }

      "when procedure type is Normal and reduced data set indicator is undefined" in {
        val initialUserAnswers = emptyUserAnswers
          .setValue(DeclarationTypePage, TIR)
          .setValue(ProcedureTypePage, Normal)
          .setValue(AddAuthorisationsYesNoPage, false)

        forAll(arbitraryTransportAnswers(initialUserAnswers)) {
          userAnswers =>
            val result = TransportDomain.userAnswersReader.run(userAnswers)
            result.value.value.authorisationsAndLimit must not be defined
            result.value.pages.last mustEqual TransportSection
        }
      }

      "when reduced data set indicator is false and procedure type is Normal" in {
        forAll(arbitrary[String](arbitraryNonTIRDeclarationType)) {
          declarationType =>
            val initialUserAnswers = emptyUserAnswers
              .setValue(DeclarationTypePage, declarationType)
              .setValue(ApprovedOperatorPage, false)
              .setValue(ProcedureTypePage, Normal)

            forAll(arbitraryTransportAnswers(initialUserAnswers)) {
              userAnswers =>
                val result = TransportDomain.userAnswersReader.run(userAnswers)
                result.value.value.authorisationsAndLimit must not be defined
                result.value.pages.last mustEqual TransportSection
            }
        }
      }

      "when reduced data set indicator is false and procedure type is Simplified" - {
        forAll(arbitrary[String](arbitraryNonTIRDeclarationType)) {
          declarationType =>
            val initialUserAnswers = emptyUserAnswers
              .setValue(DeclarationTypePage, declarationType)
              .setValue(ApprovedOperatorPage, false)
              .setValue(ProcedureTypePage, Simplified)

            forAll(arbitraryTransportAnswers(initialUserAnswers)) {
              userAnswers =>
                val result = TransportDomain.userAnswersReader.run(userAnswers)
                result.value.value.authorisationsAndLimit must be(defined)
                result.value.pages.last mustEqual TransportSection
            }
        }
      }

      "when reduced data set indicator is true and procedure type is Normal" in {
        forAll(arbitrary[String](arbitraryNonTIRDeclarationType)) {
          declarationType =>
            val initialUserAnswers = emptyUserAnswers
              .setValue(DeclarationTypePage, declarationType)
              .setValue(ApprovedOperatorPage, true)
              .setValue(ProcedureTypePage, Normal)

            forAll(arbitraryTransportAnswers(initialUserAnswers)) {
              userAnswers =>
                val result = TransportDomain.userAnswersReader.run(userAnswers)
                result.value.value.authorisationsAndLimit must not be defined
                result.value.pages.last mustEqual TransportSection
            }
        }
      }

      "when adding supply chain actors" in {
        val initialUserAnswers = emptyUserAnswers.setValue(SupplyChainActorYesNoPage, true)
        forAll(arbitraryTransportAnswers(initialUserAnswers)) {
          userAnswers =>
            val result = TransportDomain.userAnswersReader.run(userAnswers)
            result.value.value.supplyChainActors must be(defined)
            result.value.pages.last mustEqual TransportSection
        }
      }

      "when not adding supply chain actors" in {
        val initialUserAnswers = emptyUserAnswers.setValue(SupplyChainActorYesNoPage, false)
        forAll(arbitraryTransportAnswers(initialUserAnswers)) {
          userAnswers =>
            val result = TransportDomain.userAnswersReader.run(userAnswers)
            result.value.value.supplyChainActors must not be defined
            result.value.pages.last mustEqual TransportSection
        }
      }

      "when adding carrier details" in {
        val initialUserAnswers = emptyUserAnswers.setValue(CarrierDetailYesNoPage, true)
        forAll(arbitraryTransportAnswers(initialUserAnswers)) {
          userAnswers =>
            val result = TransportDomain.userAnswersReader.run(userAnswers)
            result.value.value.carrierDetails must be(defined)
            result.value.pages.last mustEqual TransportSection
        }
      }

      "when not adding carrier details" in {
        val initialUserAnswers = emptyUserAnswers.setValue(CarrierDetailYesNoPage, false)
        forAll(arbitraryTransportAnswers(initialUserAnswers)) {
          userAnswers =>
            val result = TransportDomain.userAnswersReader.run(userAnswers)
            result.value.value.carrierDetails must not be defined
            result.value.pages.last mustEqual TransportSection
        }
      }

      "when adding an additional reference" in {
        val initialUserAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceYesNoPage, true)
        forAll(arbitraryTransportAnswers(initialUserAnswers)) {
          userAnswers =>
            val result = TransportDomain.userAnswersReader.run(userAnswers)
            result.value.value.additionalReferences must be(defined)
            result.value.pages.last mustEqual TransportSection
        }
      }

      "when not adding an additional reference" in {
        val initialUserAnswers = emptyUserAnswers.setValue(AddAdditionalReferenceYesNoPage, false)
        forAll(arbitraryTransportAnswers(initialUserAnswers)) {
          userAnswers =>
            val result = TransportDomain.userAnswersReader.run(userAnswers)
            result.value.value.additionalReferences must not be defined
            result.value.pages.last mustEqual TransportSection
        }
      }

      "when adding an additional information" in {
        val initialUserAnswers = emptyUserAnswers.setValue(AddAdditionalInformationYesNoPage, true)
        forAll(arbitraryTransportAnswers(initialUserAnswers)) {
          userAnswers =>
            val result = TransportDomain.userAnswersReader.run(userAnswers)
            result.value.value.additionalInformations must be(defined)
            result.value.pages.last mustEqual TransportSection
        }
      }

      "when not adding an additional information" in {
        val initialUserAnswers = emptyUserAnswers.setValue(AddAdditionalInformationYesNoPage, false)
        forAll(arbitraryTransportAnswers(initialUserAnswers)) {
          userAnswers =>
            val result = TransportDomain.userAnswersReader.run(userAnswers)
            result.value.value.additionalInformations must not be defined
            result.value.pages.last mustEqual TransportSection
        }
      }
    }

    "authorisationsAndLimitReads" - {
      "can not parsed from user answers" - {
        "when procedure type is normal" in {
          val userAnswers = emptyUserAnswers
            .setValue(ProcedureTypePage, Normal)

          val result = TransportDomain.authorisationsAndLimitReads.apply(Nil).run(userAnswers)
          result.value.value must not be defined
          result.value.pages mustEqual Nil
        }
      }

      "can not be parsed from user answers" - {
        "when procedure type is simplified" - {
          "and inference is not flagged as true" in {
            val userAnswers = emptyUserAnswers
              .setValue(ProcedureTypePage, Simplified)

            val result = TransportDomain.authorisationsAndLimitReads.apply(Nil).run(userAnswers)
            result.left.value.page mustEqual AuthorisationsInferredPage
            result.left.value.pages mustEqual Seq(
              AuthorisationsInferredPage
            )
          }
        }
      }
    }
  }
}
