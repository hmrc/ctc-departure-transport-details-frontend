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
            result.value mustBe AdditionalReference(additionalReferenceType, Some(additionalReferenceNumber))
        }
      }

      "when only type defined" in {
        forAll(arbitrary[AdditionalReferenceType]) {
          additionalReferenceType =>
            val userAnswers = emptyUserAnswers
              .setValue(AdditionalReferenceTypePage(index), additionalReferenceType)

            val result = AdditionalReference(userAnswers, index)
            result.value mustBe AdditionalReference(additionalReferenceType, None)
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
            result mustBe None
        }
      }

      "when neither type nor number defined" in {
        val result = AdditionalReference(emptyUserAnswers, index)
        result mustBe None
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
            result mustBe s"${additionalReferenceType.documentType} - ${additionalReferenceType.description}"
        }
      }

      "when type and number defined" in {
        forAll(arbitrary[AdditionalReferenceType], nonEmptyString) {
          (additionalReferenceType, additionalReferenceNumber) =>
            val additionalReference = AdditionalReference(additionalReferenceType, Some(additionalReferenceNumber))
            val result              = additionalReference.forRemoveDisplay
            result mustBe s"${additionalReferenceType.documentType} - ${additionalReferenceType.description} - $additionalReferenceNumber"
        }
      }
    }
  }
}
