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

package controllers.transportMeans.active

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.EnumerableFormProvider
import generators.Generators
import models.transportMeans.BorderModeOfTransport
import models.transportMeans.active.Identification
import models.{NormalMode, UserAnswers}
import navigation.TransportMeansActiveNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.active.{IdentificationPage, InferredIdentificationPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.InferenceService
import views.html.transportMeans.active.IdentificationView

import scala.concurrent.Future

class IdentificationControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val formProvider             = new EnumerableFormProvider()
  private val form                     = formProvider[Identification]("transportMeans.active.identification")
  private val mode                     = NormalMode
  private lazy val identificationRoute = routes.IdentificationController.onPageLoad(lrn, mode, activeIndex).url

  private val mockInferenceService = mock[InferenceService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportMeansActiveNavigatorProvider]).toInstance(fakeTransportMeansActiveNavigatorProvider))
      .overrides(bind(classOf[InferenceService]).toInstance(mockInferenceService))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockInferenceService)
    when(mockInferenceService.inferActiveIdentifier(any(), any())).thenReturn(None)
  }

  "Identification Controller" - {

    "when value is inferred" - {
      "must redirect to next page" in {
        forAll(arbitrary[Identification]) {
          identifier =>
            beforeEach()
            when(mockInferenceService.inferActiveIdentifier(any(), any())).thenReturn(Some(identifier))

            setExistingUserAnswers(emptyUserAnswers)

            val request = FakeRequest(GET, identificationRoute)

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual onwardRoute.url

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
            userAnswersCaptor.getValue.getValue(InferredIdentificationPage(activeIndex)) mustBe identifier
            userAnswersCaptor.getValue.get(IdentificationPage(activeIndex)) must not be defined
        }
      }
    }

    "must return OK and the correct view for a GET" in {
      forAll(arbitrary[BorderModeOfTransport]) {
        borderModeOfTransport =>
          val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, borderModeOfTransport)

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, identificationRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[IdentificationView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, lrn, Identification.radioItemsU(userAnswers, activeIndex), mode, activeIndex)(request, messages).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers.setValue(IdentificationPage(activeIndex), Identification.values.head)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, identificationRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> Identification.values.head.toString))

      val view = injector.instanceOf[IdentificationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, Identification.radioItemsU(userAnswers), mode, activeIndex)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, identificationRoute)
        .withFormUrlEncodedBody(("value", Identification.values.head.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = emptyUserAnswers
      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, identificationRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[IdentificationView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, Identification.radioItemsU(userAnswers), mode, activeIndex)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, identificationRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, identificationRoute)
        .withFormUrlEncodedBody(("value", Identification.values.head.toString))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
