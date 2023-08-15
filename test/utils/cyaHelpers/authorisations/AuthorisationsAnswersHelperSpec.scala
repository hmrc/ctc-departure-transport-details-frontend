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

package utils.cyaHelpers.authorisations

import base.SpecBase
import controllers.authorisationsAndLimit.authorisations.index.routes
import generators.Generators
import models.{Index, Mode}
import models.authorisations.AuthorisationType
import models.domain.UserAnswersReader
import models.journeyDomain.authorisationsAndLimit.authorisations.AuthorisationDomain
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.authorisationsAndLimit.authorisations.AddAuthorisationsYesNoPage
import pages.authorisationsAndLimit.authorisations.index.{AuthorisationReferenceNumberPage, AuthorisationTypePage, InferredAuthorisationTypePage}
import viewModels.ListItem

class AuthorisationsAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "AuthorisationsAnswersHelperSpec" - {

    "listItems" - {

      "when empty user answers" - {
        "must return empty list of list items" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val userAnswers = emptyUserAnswers

              val helper = new AuthorisationsAnswersHelper(userAnswers, mode)
              helper.listItems mustBe Nil
          }
        }
      }

      "when user answers populated with a complete authorisation" - {
        "and at index 0 and add authorisation yes/no page is defined and true" - {
          "must return one list item with remove link" in {
            val initialAnswers = emptyUserAnswers.setValue(AddAuthorisationsYesNoPage, true)
            forAll(arbitrary[Mode], arbitraryAuthorisationAnswers(initialAnswers, authorisationIndex)) {
              (mode, userAnswers) =>
                val authorisation = UserAnswersReader[AuthorisationDomain](AuthorisationDomain.userAnswersReader(authorisationIndex)).run(userAnswers).value
                val helper        = new AuthorisationsAnswersHelper(userAnswers, mode)

                helper.listItems mustBe Seq(
                  Right(
                    ListItem(
                      name = authorisation.asString,
                      changeUrl = routes.AuthorisationReferenceNumberController.onPageLoad(userAnswers.lrn, mode, authorisationIndex).url,
                      removeUrl = Some(routes.RemoveAuthorisationYesNoController.onPageLoad(userAnswers.lrn, mode, authorisationIndex).url)
                    )
                  )
                )
            }
          }
        }

        "and at index 1 and add authorisations yes/no page is undefined and one auth type inferred" - {
          "must return one list item with no remove link" in {
            forAll(arbitrary[Mode], arbitrary[AuthorisationType], nonEmptyString) {
              (mode, authType, reference) =>
                val userAnswers = emptyUserAnswers
                  .setValue(InferredAuthorisationTypePage(Index(0)), authType)
                  .setValue(AuthorisationReferenceNumberPage(Index(0)), reference)
                  .setValue(AuthorisationTypePage(Index(1)), authType)
                  .setValue(AuthorisationReferenceNumberPage(Index(1)), reference)

                val helper = new AuthorisationsAnswersHelper(userAnswers, mode)

                helper.listItems mustBe Seq(
                  Right(
                    ListItem(
                      name = s"${authType.forDisplay} - $reference",
                      changeUrl = routes.AuthorisationReferenceNumberController.onPageLoad(userAnswers.lrn, mode, authorisationIndex).url,
                      removeUrl = None
                    )
                  ),
                  Right(
                    ListItem(
                      name = s"${authType.forDisplay} - $reference",
                      changeUrl = routes.AuthorisationReferenceNumberController.onPageLoad(userAnswers.lrn, mode, Index(1)).url,
                      removeUrl = Some(routes.RemoveAuthorisationYesNoController.onPageLoad(userAnswers.lrn, mode, Index(1)).url)
                    )
                  )
                )
            }
          }
        }

        "and auth type for index 0 and 1 have been inferred" - {
          "must return two list item with no remove link" in {
            forAll(arbitrary[Mode], arbitrary[AuthorisationType], nonEmptyString) {
              (mode, authType, reference) =>
                val userAnswers = emptyUserAnswers
                  .setValue(InferredAuthorisationTypePage(Index(0)), authType)
                  .setValue(AuthorisationReferenceNumberPage(Index(0)), reference)
                  .setValue(InferredAuthorisationTypePage(Index(1)), authType)
                  .setValue(AuthorisationReferenceNumberPage(Index(1)), reference)
                val helper = new AuthorisationsAnswersHelper(userAnswers, mode)

                helper.listItems mustBe Seq(
                  Right(
                    ListItem(
                      name = s"${authType.forDisplay} - $reference",
                      changeUrl = routes.AuthorisationReferenceNumberController.onPageLoad(userAnswers.lrn, mode, Index(0)).url,
                      removeUrl = None
                    )
                  ),
                  Right(
                    ListItem(
                      name = s"${authType.forDisplay} - $reference",
                      changeUrl = routes.AuthorisationReferenceNumberController.onPageLoad(userAnswers.lrn, mode, Index(1)).url,
                      removeUrl = None
                    )
                  )
                )
            }
          }
        }
      }
    }
  }
}
