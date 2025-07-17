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
import models.reference.authorisations.AuthorisationType
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.authorisationsAndLimit.AddAuthorisationsYesNoPage
import pages.authorisationsAndLimit.authorisations.index.{
  AuthorisationReferenceNumberPage,
  AuthorisationTypePage,
  InferredAuthorisationTypePage,
  IsMandatoryPage
}
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
              helper.listItems mustEqual Nil
          }
        }
      }

      "when user answers populated with a complete authorisation" - {
        "and section is mandatory" - {
          "and we have one authorisation" - {
            "and it is inferred" in {
              forAll(arbitrary[Mode], arbitrary[AuthorisationType], nonEmptyString) {
                (mode, authType, reference) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(InferredAuthorisationTypePage(Index(0)), authType)
                    .setValue(IsMandatoryPage(Index(0)), true)
                    .setValue(AuthorisationReferenceNumberPage(Index(0)), reference)

                  val helper = new AuthorisationsAnswersHelper(userAnswers, mode)

                  helper.listItems mustEqual Seq(
                    Right(
                      ListItem(
                        name = s"${authType.forDisplay} - $reference",
                        changeUrl = routes.AuthorisationReferenceNumberController.onPageLoad(userAnswers.lrn, mode, Index(0)).url,
                        removeUrl = None
                      )
                    )
                  )
              }
            }

            "and it is not inferred" in {
              forAll(arbitrary[Mode], arbitrary[AuthorisationType], nonEmptyString) {
                (mode, authType, reference) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(AuthorisationTypePage(Index(0)), authType)
                    .setValue(AuthorisationReferenceNumberPage(Index(0)), reference)

                  val helper = new AuthorisationsAnswersHelper(userAnswers, mode)

                  helper.listItems mustEqual Seq(
                    Right(
                      ListItem(
                        name = s"${authType.forDisplay} - $reference",
                        changeUrl = routes.AuthorisationReferenceNumberController.onPageLoad(userAnswers.lrn, mode, Index(0)).url,
                        removeUrl = None
                      )
                    )
                  )
              }
            }
          }

          "and we have multiple authorisations" - {
            "and all are inferred" in {
              forAll(arbitrary[Mode], arbitrary[AuthorisationType], nonEmptyString) {
                (mode, authType, reference) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(InferredAuthorisationTypePage(Index(0)), authType)
                    .setValue(IsMandatoryPage(Index(0)), true)
                    .setValue(AuthorisationReferenceNumberPage(Index(0)), reference)
                    .setValue(InferredAuthorisationTypePage(Index(1)), authType)
                    .setValue(IsMandatoryPage(Index(1)), true)
                    .setValue(AuthorisationReferenceNumberPage(Index(1)), reference)

                  val helper = new AuthorisationsAnswersHelper(userAnswers, mode)

                  helper.listItems mustEqual Seq(
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

            "and none are inferred" in {
              forAll(arbitrary[Mode], arbitrary[AuthorisationType], nonEmptyString) {
                (mode, authType, reference) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(AuthorisationTypePage(Index(0)), authType)
                    .setValue(AuthorisationReferenceNumberPage(Index(0)), reference)
                    .setValue(AuthorisationTypePage(Index(1)), authType)
                    .setValue(AuthorisationReferenceNumberPage(Index(1)), reference)

                  val helper = new AuthorisationsAnswersHelper(userAnswers, mode)

                  helper.listItems mustEqual Seq(
                    Right(
                      ListItem(
                        name = s"${authType.forDisplay} - $reference",
                        changeUrl = routes.AuthorisationReferenceNumberController.onPageLoad(userAnswers.lrn, mode, Index(0)).url,
                        removeUrl = Some(routes.RemoveAuthorisationYesNoController.onPageLoad(userAnswers.lrn, mode, Index(0)).url)
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
        }

        "and section is optional" - {
          "and we have one authorisation" - {
            "and it is inferred" in {
              forAll(arbitrary[Mode], arbitrary[AuthorisationType], nonEmptyString) {
                (mode, authType, reference) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(AddAuthorisationsYesNoPage, true)
                    .setValue(InferredAuthorisationTypePage(Index(0)), authType)
                    .setValue(IsMandatoryPage(Index(0)), true)
                    .setValue(AuthorisationReferenceNumberPage(Index(0)), reference)

                  val helper = new AuthorisationsAnswersHelper(userAnswers, mode)

                  helper.listItems mustEqual Seq(
                    Right(
                      ListItem(
                        name = s"${authType.forDisplay} - $reference",
                        changeUrl = routes.AuthorisationReferenceNumberController.onPageLoad(userAnswers.lrn, mode, Index(0)).url,
                        removeUrl = None
                      )
                    )
                  )
              }
            }

            "and it is not inferred" in {
              forAll(arbitrary[Mode], arbitrary[AuthorisationType], nonEmptyString) {
                (mode, authType, reference) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(AddAuthorisationsYesNoPage, true)
                    .setValue(AuthorisationTypePage(Index(0)), authType)
                    .setValue(AuthorisationReferenceNumberPage(Index(0)), reference)

                  val helper = new AuthorisationsAnswersHelper(userAnswers, mode)

                  helper.listItems mustEqual Seq(
                    Right(
                      ListItem(
                        name = s"${authType.forDisplay} - $reference",
                        changeUrl = routes.AuthorisationReferenceNumberController.onPageLoad(userAnswers.lrn, mode, Index(0)).url,
                        removeUrl = Some(routes.RemoveAuthorisationYesNoController.onPageLoad(userAnswers.lrn, mode, Index(0)).url)
                      )
                    )
                  )
              }
            }
          }

          "and we have multiple authorisations" - {
            "and all are inferred" in {
              forAll(arbitrary[Mode], arbitrary[AuthorisationType], nonEmptyString) {
                (mode, authType, reference) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(AddAuthorisationsYesNoPage, true)
                    .setValue(InferredAuthorisationTypePage(Index(0)), authType)
                    .setValue(IsMandatoryPage(Index(0)), true)
                    .setValue(AuthorisationReferenceNumberPage(Index(0)), reference)
                    .setValue(InferredAuthorisationTypePage(Index(1)), authType)
                    .setValue(IsMandatoryPage(Index(1)), true)
                    .setValue(AuthorisationReferenceNumberPage(Index(1)), reference)

                  val helper = new AuthorisationsAnswersHelper(userAnswers, mode)

                  helper.listItems mustEqual Seq(
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

            "and none are inferred" in {
              forAll(arbitrary[Mode], arbitrary[AuthorisationType], nonEmptyString) {
                (mode, authType, reference) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(AddAuthorisationsYesNoPage, true)
                    .setValue(AuthorisationTypePage(Index(0)), authType)
                    .setValue(AuthorisationReferenceNumberPage(Index(0)), reference)
                    .setValue(AuthorisationTypePage(Index(1)), authType)
                    .setValue(AuthorisationReferenceNumberPage(Index(1)), reference)

                  val helper = new AuthorisationsAnswersHelper(userAnswers, mode)

                  helper.listItems mustEqual Seq(
                    Right(
                      ListItem(
                        name = s"${authType.forDisplay} - $reference",
                        changeUrl = routes.AuthorisationReferenceNumberController.onPageLoad(userAnswers.lrn, mode, Index(0)).url,
                        removeUrl = Some(routes.RemoveAuthorisationYesNoController.onPageLoad(userAnswers.lrn, mode, Index(0)).url)
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
        }
      }

      "when user answers populated with an in-progress authorisation" - {
        "and authorisation type is inferred" in {
          forAll(arbitrary[Mode], arbitrary[AuthorisationType], nonEmptyString) {
            (mode, authType, reference) =>
              val userAnswers = emptyUserAnswers
                .setValue(InferredAuthorisationTypePage(Index(0)), authType)
                .setValue(IsMandatoryPage(Index(0)), true)
                .setValue(AuthorisationReferenceNumberPage(Index(0)), reference)
                .setValue(InferredAuthorisationTypePage(Index(1)), authType)
                .setValue(IsMandatoryPage(Index(1)), true)
                .setValue(AuthorisationReferenceNumberPage(Index(1)), reference)
                .setValue(InferredAuthorisationTypePage(Index(2)), authType)
              val helper = new AuthorisationsAnswersHelper(userAnswers, mode)

              helper.listItems mustEqual Seq(
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
                ),
                Left(
                  ListItem(
                    name = authType.forDisplay,
                    changeUrl = routes.AuthorisationReferenceNumberController.onPageLoad(userAnswers.lrn, mode, Index(2)).url,
                    removeUrl = Some(routes.RemoveAuthorisationYesNoController.onPageLoad(userAnswers.lrn, mode, Index(2)).url)
                  )
                )
              )
          }
        }
      }
    }
  }
}
