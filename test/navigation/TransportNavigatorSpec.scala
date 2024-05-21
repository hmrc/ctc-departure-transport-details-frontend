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

package navigation

import base.SpecBase
import generators.Generators
import models._
import models.reference.additionalInformation.AdditionalInformationCode
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.additionalInformation.index.{AdditionalInformationTextPage, AdditionalInformationTypePage}
import pages.additionalInformation.{AddAdditionalInformationYesNoPage, AddCommentsYesNoPage}
import pages.additionalReference.AddAdditionalReferenceYesNoPage
import pages.sections.additionalInformation.AdditionalInformationListSection
import pages.sections.additionalReference.AdditionalReferencesSection

class TransportNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Transport Navigator" - {

    "when answers complete" - {
      "must redirect to transport check your answers" in {
        forAll(arbitraryTransportAnswers(emptyUserAnswers), arbitrary[Mode]) {
          (answers, mode) =>
            val navigatorProvider = new TransportNavigatorProviderImpl()
            val navigator         = navigatorProvider.apply(mode)

            navigator
              .nextPage(answers, None)
              .mustBe(controllers.routes.TransportAnswersController.onPageLoad(answers.lrn))
        }
      }
    }

    "normal mode" - {
      "AddAdditionalReferenceYesNoPage" - {
        "must redirect to AddAdditionalInformationYesNoPage when answer is No" in {
          forAll(arbitraryTransportAnswers(emptyUserAnswers)) {
            ua =>
              val navigatorProvider = new TransportNavigatorProviderImpl()
              val navigator         = navigatorProvider.apply(NormalMode)
              val currentPage       = AddAdditionalReferenceYesNoPage
              val answers           = ua.set(currentPage, false).get

              navigator
                .nextPage(answers, Some(currentPage))
                .mustBe(controllers.additionalInformation.routes.AddAdditionalInformationYesNoController.onPageLoad(answers.lrn, NormalMode))
          }
        }
      }

      "AddAnotherAdditionalReference" - {
        "must redirect to AddAdditionalInformationYesNoPage when answer is No" in {
          forAll(arbitraryTransportAnswers(emptyUserAnswers)) {
            ua =>
              val navigatorProvider = new TransportNavigatorProviderImpl()
              val navigator         = navigatorProvider.apply(NormalMode)
              val currentPage       = AdditionalReferencesSection

              navigator
                .nextPage(ua, Some(currentPage))
                .mustBe(controllers.additionalInformation.routes.AddAdditionalInformationYesNoController.onPageLoad(ua.lrn, NormalMode))
          }
        }
      }

      "AddAdditionalInformationYesNoPage" - {
        "must redirect to AdditionalInformationTypePage when answer is Yes" in {
          forAll(arbitraryTransportAnswers(emptyUserAnswers)) {
            ua =>
              val navigatorProvider = new TransportNavigatorProviderImpl()
              val navigator         = navigatorProvider.apply(NormalMode)
              val currentPage       = AddAdditionalInformationYesNoPage
              val answers           = ua.set(currentPage, true).get

              navigator
                .nextPage(answers, Some(currentPage))
                .mustBe(controllers.additionalInformation.index.routes.AdditionalInformationTypeController.onPageLoad(Index(0), answers.lrn, NormalMode))
          }
        }

        "must redirect to CYA when answer is No" in {
          forAll(arbitraryTransportAnswers(emptyUserAnswers)) {
            ua =>
              val navigatorProvider = new TransportNavigatorProviderImpl()
              val navigator         = navigatorProvider.apply(NormalMode)
              val currentPage       = AddAdditionalInformationYesNoPage
              val answers           = ua.set(currentPage, false).get

              navigator
                .nextPage(answers, Some(currentPage))
                .mustBe(controllers.routes.TransportAnswersController.onPageLoad(answers.lrn))
          }
        }
      }

      "AdditionalInformationTypePage" - {
        "must redirect to AddCommentsYesNoPage when answer is No" in {
          forAll(arbitraryTransportAnswers(emptyUserAnswers)) {
            ua =>
              val navigatorProvider = new TransportNavigatorProviderImpl()
              val navigator         = navigatorProvider.apply(NormalMode)
              val currentPage       = AdditionalInformationTypePage(index)
              val answers           = ua.set(currentPage, AdditionalInformationCode("code", "description")).get

              navigator
                .nextPage(answers, Some(currentPage))
                .mustBe(controllers.additionalInformation.index.routes.AddCommentsYesNoController.onPageLoad(ua.lrn, index, NormalMode))
          }
        }
      }

      "AddCommentsYesNoPage" - {
        "must redirect to AdditionalInformationTextPage when answer is Yes" in {
          forAll(arbitraryTransportAnswers(emptyUserAnswers)) {
            ua =>
              val navigatorProvider = new TransportNavigatorProviderImpl()
              val navigator         = navigatorProvider.apply(NormalMode)
              val currentPage       = AddCommentsYesNoPage(index)
              val answers           = ua.set(currentPage, true).get

              navigator
                .nextPage(answers, Some(currentPage))
                .mustBe(controllers.additionalInformation.index.routes.AdditionalInformationTextController.onPageLoad(answers.lrn, index, NormalMode))
          }
        }

        "must redirect to AddAnotherAdditionalInformation when answer is No" in {
          forAll(arbitraryTransportAnswers(emptyUserAnswers)) {
            ua =>
              val navigatorProvider = new TransportNavigatorProviderImpl()
              val navigator         = navigatorProvider.apply(NormalMode)
              val currentPage       = AddCommentsYesNoPage(index)
              val answers           = ua.set(currentPage, false).get

              navigator
                .nextPage(answers, Some(currentPage))
                .mustBe(controllers.additionalInformation.routes.AddAnotherAdditionalInformationController.onPageLoad(answers.lrn, NormalMode))
          }
        }
      }

      "AdditionalInformationTextPage" - {
        "must redirect to AddAnotherAdditionalInformation" in {
          forAll(arbitraryTransportAnswers(emptyUserAnswers)) {
            ua =>
              val navigatorProvider = new TransportNavigatorProviderImpl()
              val navigator         = navigatorProvider.apply(NormalMode)
              val currentPage       = AdditionalInformationTextPage(index)
              val answers           = ua.set(currentPage, "text").get

              navigator
                .nextPage(answers, Some(currentPage))
                .mustBe(controllers.additionalInformation.routes.AddAnotherAdditionalInformationController.onPageLoad(answers.lrn, NormalMode))
          }
        }
      }

      "AddAnotherAdditionalInformation" - {
        "must redirect to CYA when answer is No" in {
          forAll(arbitraryTransportAnswers(emptyUserAnswers)) {
            ua =>
              val navigatorProvider = new TransportNavigatorProviderImpl()
              val navigator         = navigatorProvider.apply(NormalMode)
              val currentPage       = AdditionalInformationListSection

              navigator
                .nextPage(ua, Some(currentPage))
                .mustBe(controllers.routes.TransportAnswersController.onPageLoad(ua.lrn))
          }
        }
      }
    }
  }
}
