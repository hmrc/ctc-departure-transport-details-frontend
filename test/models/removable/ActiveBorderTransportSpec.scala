/*
 * Copyright 2024 HM Revenue & Customs
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

package models.removable

import base.SpecBase
import generators.Generators
import models.reference.transportMeans.active.Identification
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.active.{IdentificationNumberPage, IdentificationPage}

class ActiveBorderTransportSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "apply" - {

    "must return an border transport means" - {
      "when type and number defined" in {
        forAll(arbitrary[Identification], nonEmptyString) {
          (identification, identificationNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(IdentificationPage(index), identification)
              .setValue(IdentificationNumberPage(index), identificationNumber)

            val result = ActiveBorderTransport(userAnswers, index)
            result.value mustEqual ActiveBorderTransport(identification, Some(identificationNumber))
        }
      }

      "when only type defined" in {
        forAll(arbitrary[Identification]) {
          identification =>
            val userAnswers = emptyUserAnswers
              .setValue(IdentificationPage(index), identification)

            val result = ActiveBorderTransport(userAnswers, index)
            result.value mustEqual ActiveBorderTransport(identification, None)
        }
      }
    }

    "must not return an additional reference" - {
      "when only number defined" in {
        forAll(nonEmptyString) {
          identificationNumber =>
            val userAnswers = emptyUserAnswers
              .setValue(IdentificationNumberPage(index), identificationNumber)

            val result = ActiveBorderTransport(userAnswers, index)
            result must not be defined
        }
      }

      "when neither type nor number defined" in {
        val result = ActiveBorderTransport(emptyUserAnswers, index)
        result must not be defined
      }
    }
  }

  "forRemoveDisplay" - {
    "must cast additional reference to string" - {
      "when only type defined" in {
        forAll(arbitrary[Identification]) {
          identification =>
            val activeBorderTransport = ActiveBorderTransport(identification, None)
            val result                = activeBorderTransport.forRemoveDisplay
            result mustEqual s"${identification.description}"
        }
      }

      "when type and number defined" in {
        forAll(arbitrary[Identification], nonEmptyString) {
          (identification, identificationNumber) =>
            val additionalReference = ActiveBorderTransport(identification, Some(identificationNumber))
            val result              = additionalReference.forRemoveDisplay
            result mustEqual s"${identification.description} - $identificationNumber"
        }
      }
    }
  }
}
