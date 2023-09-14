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

package controllers.authorisationsAndLimit.authorisations.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.AuthorisationReferenceNumberFormProvider
import generators.Generators
import models.reference.authorisations.AuthorisationType
import models.{DeclarationType, NormalMode}
import navigation.AuthorisationNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.authorisationsAndLimit.authorisations.index.{AuthorisationReferenceNumberPage, AuthorisationTypePage, InferredAuthorisationTypePage}
import pages.external.{ApprovedOperatorPage, DeclarationTypePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.authorisationsAndLimit.authorisations.index.AuthorisationReferenceNumberView

import scala.concurrent.Future

class AuthorisationReferenceNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val prefix = "authorisations.authorisationReferenceNumber"

  private val formProvider                               = new AuthorisationReferenceNumberFormProvider()
  private def form(authorisationType: AuthorisationType) = formProvider(prefix, authorisationType.forDisplay)
  private val mode                                       = NormalMode
  private val validAnswer                                = "testString"

  private lazy val authorisationReferenceNumberRoute = routes.AuthorisationReferenceNumberController.onPageLoad(lrn, mode, authorisationIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[AuthorisationNavigatorProvider]).toInstance(fakeAuthorisationNavigatorProvider))

  private val authorisationTypePageGen = Gen.oneOf(AuthorisationTypePage(authorisationIndex), InferredAuthorisationTypePage(authorisationIndex))

  private val baseAnswers = {
    val declarationType = arbitrary[DeclarationType](arbitraryNonOption4DeclarationType).sample.value
    emptyUserAnswers.setValue(DeclarationTypePage, declarationType)
  }

  "AuthorisationReferenceNumber Controller" - {

    "must return OK and the correct view for a GET" in {
      forAll(arbitrary[AuthorisationType], authorisationTypePageGen, arbitrary[Boolean]) {
        (authorisationType, page, isReducedDataset) =>
          val userAnswers = baseAnswers
            .setValue(ApprovedOperatorPage, isReducedDataset)
            .setValue(page, authorisationType)
          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, authorisationReferenceNumberRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[AuthorisationReferenceNumberView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form(authorisationType), lrn, authorisationType.forDisplay, mode, index, isReducedDataset)(request, messages).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      forAll(arbitrary[AuthorisationType], authorisationTypePageGen, arbitrary[Boolean]) {
        (authorisationType, page, isReducedDataset) =>
          val userAnswers = baseAnswers
            .setValue(ApprovedOperatorPage, isReducedDataset)
            .setValue(page, authorisationType)
            .setValue(AuthorisationReferenceNumberPage(authorisationIndex), validAnswer)
          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, authorisationReferenceNumberRoute)

          val result = route(app, request).value

          val filledForm = form(authorisationType).bind(Map("value" -> validAnswer))

          val view = injector.instanceOf[AuthorisationReferenceNumberView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(filledForm, lrn, authorisationType.forDisplay, mode, index, isReducedDataset)(request, messages).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      forAll(arbitrary[AuthorisationType], authorisationTypePageGen, arbitrary[Boolean]) {
        (authorisationType, page, isReducedDataset) =>
          val userAnswers = baseAnswers
            .setValue(ApprovedOperatorPage, isReducedDataset)
            .setValue(page, authorisationType)
          setExistingUserAnswers(userAnswers)

          when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

          val request = FakeRequest(POST, authorisationReferenceNumberRoute)
            .withFormUrlEncodedBody(("value", validAnswer))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      forAll(arbitrary[AuthorisationType], authorisationTypePageGen, arbitrary[Boolean]) {
        (authorisationType, page, isReducedDataset) =>
          val userAnswers = baseAnswers
            .setValue(ApprovedOperatorPage, isReducedDataset)
            .setValue(page, authorisationType)
          setExistingUserAnswers(userAnswers)

          val invalidAnswer = ""

          val request    = FakeRequest(POST, authorisationReferenceNumberRoute).withFormUrlEncodedBody(("value", invalidAnswer))
          val filledForm = form(authorisationType).bind(Map("value" -> invalidAnswer))

          val result = route(app, request).value

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[AuthorisationReferenceNumberView]

          contentAsString(result) mustEqual
            view(filledForm, lrn, authorisationType.forDisplay, mode, authorisationIndex, isReducedDataset)(request, messages).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, authorisationReferenceNumberRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, authorisationReferenceNumberRoute)
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
