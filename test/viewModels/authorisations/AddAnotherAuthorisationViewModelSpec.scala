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

package viewModels.authorisations

import base.SpecBase
import generators.Generators
import models.reference.authorisations.AuthorisationType
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.authorisations.AddAnotherAuthorisationViewModel.AddAnotherAuthorisationViewModelProvider

class AddAnotherAuthorisationViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must get list items" - {

    "when there is one authorisation" in {
      forAll(arbitrary[Mode]) {
        mode =>
          val userAnswers = arbitraryAuthorisationAnswers(emptyUserAnswers, authorisationIndex).sample.value

          val result = new AddAnotherAuthorisationViewModelProvider().apply(userAnswers, mode, Seq(AuthorisationType("foo", "bar")))

          result.listItems.length mustEqual 1
          result.title mustEqual "You have added 1 authorisation"
          result.heading mustEqual "You have added 1 authorisation"
          result.legend mustEqual "Do you want to add another authorisation?"
          result.maxLimitLabel mustEqual "You cannot add any more authorisations. To add another, you need to remove one first."
          result.allowMore mustEqual true
      }
    }

    "when there are multiple authorisations" in {
      val formatter = java.text.NumberFormat.getIntegerInstance

      forAll(arbitrary[Mode], Gen.choose(2, frontendAppConfig.maxAuthorisations)) {
        (mode, authorisations) =>
          val userAnswers = (0 until authorisations).foldLeft(emptyUserAnswers) {
            (acc, i) =>
              arbitraryAuthorisationAnswers(acc, Index(i)).sample.value
          }

          val result = new AddAnotherAuthorisationViewModelProvider().apply(userAnswers, mode, Nil)
          result.listItems.length mustEqual authorisations
          result.title mustEqual s"You have added ${formatter.format(authorisations)} authorisations"
          result.heading mustEqual s"You have added ${formatter.format(authorisations)} authorisations"
          result.legend mustEqual "Do you want to add another authorisation?"
          result.maxLimitLabel mustEqual "You cannot add any more authorisations. To add another, you need to remove one first."
          result.allowMore mustEqual false
      }
    }
  }
}
