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
import models.reference.additionalReference.AdditionalReferenceType
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalReference.index._

class AdditionalReferenceSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "apply" - {

    "must return an additional reference" - {
      "when type and number defined" in {
        forAll(arbitrary[AdditionalReferenceType], nonEmptyString) {
          (additionalReferenceType, additionalReferenceNumber) =>
            val userAnswers = emptyUserAnswers
              .setValue(AdditionalReferenceTypePage(index), additionalReferenceType)
              .setValue(AdditionalReferenceNumberPage(index), additionalReferenceNumber)

            val result = AdditionalReference(userAnswers, index)
            result.value mustEqual AdditionalReference(additionalReferenceType, Some(additionalReferenceNumber))
        }
      }

      "when only type defined" in {
        forAll(arbitrary[AdditionalReferenceType]) {
          additionalReferenceType =>
            val userAnswers = emptyUserAnswers
              .setValue(AdditionalReferenceTypePage(index), additionalReferenceType)

            val result = AdditionalReference(userAnswers, index)
            result.value mustEqual AdditionalReference(additionalReferenceType, None)
        }
      }
    }

    "must not return an additional reference" - {
      "when only number defined" in {
        forAll(nonEmptyString) {
          additionalReferenceNumber =>
            val userAnswers = emptyUserAnswers
              .setValue(AdditionalReferenceNumberPage(index), additionalReferenceNumber)

            val result = AdditionalReference(userAnswers, index)
            result must not be defined
        }
      }

      "when neither type nor number defined" in {
        val result = AdditionalReference(emptyUserAnswers, index)
        result must not be defined
      }
    }
  }

  "forRemoveDisplay" - {
    "must cast additional reference to string" - {
      "when only type defined" in {
        forAll(arbitrary[AdditionalReferenceType]) {
          additionalReferenceType =>
            val additionalReference = AdditionalReference(additionalReferenceType, None)
            val result              = additionalReference.forRemoveDisplay
            result mustEqual s"${additionalReferenceType.documentType} - ${additionalReferenceType.description}"
        }
      }

      "when type and number defined" in {
        forAll(arbitrary[AdditionalReferenceType], nonEmptyString) {
          (additionalReferenceType, additionalReferenceNumber) =>
            val additionalReference = AdditionalReference(additionalReferenceType, Some(additionalReferenceNumber))
            val result              = additionalReference.forRemoveDisplay
            result mustEqual s"${additionalReferenceType.documentType} - ${additionalReferenceType.description} - $additionalReferenceNumber"
        }
      }
    }
  }
}
