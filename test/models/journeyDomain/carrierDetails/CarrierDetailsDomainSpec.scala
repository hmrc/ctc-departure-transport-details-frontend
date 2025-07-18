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

package models.journeyDomain.carrierDetails

import base.SpecBase
import commonTestUtils.UserAnswersSpecHelper
import generators.Generators
import org.scalacheck.Gen
import pages.carrierDetails._
import pages.carrierDetails.contact._

class CarrierDetailsDomainSpec extends SpecBase with UserAnswersSpecHelper with Generators {

  private val identificationNumber = Gen.alphaNumStr.sample.value
  private val name                 = Gen.alphaNumStr.sample.value
  private val telephoneNumber      = Gen.alphaNumStr.sample.value

  "CarrierDetailsDomain" - {

    "can be parsed from UserAnswers" - {

      "when contact person has been provided" in {
        val userAnswers = emptyUserAnswers
          .setValue(IdentificationNumberPage, identificationNumber)
          .setValue(AddContactYesNoPage, true)
          .setValue(NamePage, name)
          .setValue(TelephoneNumberPage, telephoneNumber)

        val expectedResult = CarrierDetailsDomain(
          identificationNumber = identificationNumber,
          contactPerson = Some(
            ContactPersonDomain(
              name = name,
              telephoneNumber = telephoneNumber
            )
          )
        )

        val result = CarrierDetailsDomain.userAnswersReader.apply(Nil).run(userAnswers)

        result.value.value mustEqual expectedResult
        result.value.pages mustEqual Seq(
          IdentificationNumberPage,
          AddContactYesNoPage,
          NamePage,
          TelephoneNumberPage
        )
      }

      "when contact person has not been provided" in {
        val userAnswers = emptyUserAnswers
          .setValue(IdentificationNumberPage, identificationNumber)
          .setValue(AddContactYesNoPage, false)

        val expectedResult = CarrierDetailsDomain(
          identificationNumber = identificationNumber,
          contactPerson = None
        )

        val result = CarrierDetailsDomain.userAnswersReader.apply(Nil).run(userAnswers)

        result.value.value mustEqual expectedResult
        result.value.pages mustEqual Seq(
          IdentificationNumberPage,
          AddContactYesNoPage
        )
      }
    }

    "cannot be parsed from UserAnswers" - {

      "when identification number has not been answered" in {
        val userAnswers = emptyUserAnswers

        val result = CarrierDetailsDomain.userAnswersReader.apply(Nil).run(userAnswers)

        result.left.value.page mustEqual IdentificationNumberPage
        result.left.value.pages mustEqual Seq(
          IdentificationNumberPage
        )
      }

      "when add contact person yes/no has not been answered" in {
        val userAnswers = emptyUserAnswers
          .setValue(IdentificationNumberPage, identificationNumber)

        val result = CarrierDetailsDomain.userAnswersReader.apply(Nil).run(userAnswers)

        result.left.value.page mustEqual AddContactYesNoPage
        result.left.value.pages mustEqual Seq(
          IdentificationNumberPage,
          AddContactYesNoPage
        )
      }
    }
  }
}
