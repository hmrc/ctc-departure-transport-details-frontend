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
import forms.EnumerableFormProvider
import generators.Generators
import models.reference.authorisations.AuthorisationType
import models.{DeclarationType, Index, NormalMode, UserAnswers}
import navigation.AuthorisationNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.authorisationsAndLimit.authorisations.index.{AuthorisationTypePage, InferredAuthorisationTypePage}
import pages.external.DeclarationTypePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuthorisationTypesService
import views.html.authorisationsAndLimit.authorisations.index.AuthorisationTypeView

import scala.concurrent.Future

class AuthorisationTypeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val authorisationType1 = arbitrary[AuthorisationType].sample.value
  private val authorisationType2 = arbitrary[AuthorisationType].sample.value
  private val authorisationType3 = arbitrary[AuthorisationType].sample.value
  private val authorisationTypes = Seq(authorisationType1, authorisationType2, authorisationType3)

  private val formProvider                                = new EnumerableFormProvider()
  private val form                                        = formProvider[AuthorisationType]("authorisations.authorisationType", authorisationTypes)
  private val mode                                        = NormalMode
  private lazy val authorisationTypeRoute                 = authorisationTypeRouteAtIndex(index)
  private def authorisationTypeRouteAtIndex(index: Index) = routes.AuthorisationTypeController.onPageLoad(lrn, mode, index).url

  private val mockAuthorisationTypesService: AuthorisationTypesService =
    mock[AuthorisationTypesService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthorisationTypesService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[AuthorisationNavigatorProvider]).toInstance(fakeAuthorisationNavigatorProvider))
      .overrides(bind(classOf[AuthorisationTypesService]).toInstance(mockAuthorisationTypesService))

  "AuthorisationType Controller" - {

    "when value is inferred" - {
      "must redirect to next page" in {
        when(mockAuthorisationTypesService.getAuthorisationTypes(any(), any())(any()))
          .thenReturn(Future.successful(Seq(authorisationType3)))

        val index = Index(2)

        val userAnswers = emptyUserAnswers
          .setValue(AuthorisationTypePage(Index(0)), authorisationType1)
          .setValue(AuthorisationTypePage(Index(1)), authorisationType2)
          .setValue(DeclarationTypePage, DeclarationType.Option1)
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, authorisationTypeRouteAtIndex(index))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
        userAnswersCaptor.getValue.getValue(InferredAuthorisationTypePage(index)) mustBe authorisationType3
        userAnswersCaptor.getValue.get(AuthorisationTypePage(index)) must not be defined
      }
    }

    "must return OK and the correct view for a GET" in {
      when(mockAuthorisationTypesService.getAuthorisationTypes(any(), any())(any()))
        .thenReturn(Future.successful(authorisationTypes))

      val userAnswers = emptyUserAnswers
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, authorisationTypeRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[AuthorisationTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, authorisationTypes, mode, index)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      when(mockAuthorisationTypesService.getAuthorisationTypes(any(), any())(any()))
        .thenReturn(Future.successful(authorisationTypes))

      val userAnswers = emptyUserAnswers.setValue(AuthorisationTypePage(index), authorisationTypes.head)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, authorisationTypeRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> authorisationTypes.head.toString))

      val view = injector.instanceOf[AuthorisationTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, authorisationTypes, mode, index)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockAuthorisationTypesService.getAuthorisationTypes(any(), any())(any()))
        .thenReturn(Future.successful(authorisationTypes))

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, authorisationTypeRoute)
        .withFormUrlEncodedBody(("value", authorisationTypes.head.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      when(mockAuthorisationTypesService.getAuthorisationTypes(any(), any())(any()))
        .thenReturn(Future.successful(authorisationTypes))

      val userAnswers = emptyUserAnswers
      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, authorisationTypeRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[AuthorisationTypeView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, authorisationTypes, mode, index)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, authorisationTypeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, authorisationTypeRoute)
        .withFormUrlEncodedBody(("value", authorisationTypes.head.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
