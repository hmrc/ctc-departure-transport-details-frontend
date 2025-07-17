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
import models.reference.transportMeans.active.Identification
import models.reference.BorderMode
import models.{NormalMode, UserAnswers}
import navigation.TransportMeansActiveNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.active.{IdentificationPage, InferredIdentificationPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.MeansOfTransportIdentificationTypesActiveService
import views.html.transportMeans.active.IdentificationView

import scala.concurrent.Future

class IdentificationControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val identificationType1 = Identification("40", "IATA flight number")
  private val identificationType2 = Identification("41", "Registration number of an aircraft")
  private val identificationTypes = Seq(identificationType1, identificationType2)

  private val formProvider             = new EnumerableFormProvider()
  private val form                     = formProvider[Identification]("transportMeans.active.identification", identificationTypes)
  private val mode                     = NormalMode
  private lazy val identificationRoute = routes.IdentificationController.onPageLoad(lrn, mode, activeIndex).url

  private val mockMeansOfTransportIdentificationTypesActiveService: MeansOfTransportIdentificationTypesActiveService =
    mock[MeansOfTransportIdentificationTypesActiveService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMeansOfTransportIdentificationTypesActiveService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportMeansActiveNavigatorProvider]).toInstance(fakeTransportMeansActiveNavigatorProvider))
      .overrides(bind(classOf[MeansOfTransportIdentificationTypesActiveService]).toInstance(mockMeansOfTransportIdentificationTypesActiveService))

  "Identification Controller" - {

    "when value is inferred" - {
      "must redirect to next page" in {
        when(mockMeansOfTransportIdentificationTypesActiveService.getMeansOfTransportIdentificationTypesActive(any(), any())(any()))
          .thenReturn(Future.successful(Seq(identificationType1)))

        val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, BorderMode("2", "Rail"))
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, identificationRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
        userAnswersCaptor.getValue.getValue(InferredIdentificationPage(activeIndex)) mustEqual identificationType1
        userAnswersCaptor.getValue.get(IdentificationPage(activeIndex)) must not be defined
      }
    }

    "must return OK and the correct view for a GET" in {
      when(mockMeansOfTransportIdentificationTypesActiveService.getMeansOfTransportIdentificationTypesActive(any(), any())(any()))
        .thenReturn(Future.successful(identificationTypes))

      val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, BorderMode("1", "Maritime"))

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, identificationRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[IdentificationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, identificationTypes, mode, activeIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      when(mockMeansOfTransportIdentificationTypesActiveService.getMeansOfTransportIdentificationTypesActive(any(), any())(any()))
        .thenReturn(Future.successful(identificationTypes))

      val userAnswers = emptyUserAnswers
        .setValue(BorderModeOfTransportPage, BorderMode("4", "Air"))
        .setValue(IdentificationPage(activeIndex), identificationType1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, identificationRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> identificationType1.code))

      val view = injector.instanceOf[IdentificationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, identificationTypes, mode, activeIndex)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))
      when(mockMeansOfTransportIdentificationTypesActiveService.getMeansOfTransportIdentificationTypesActive(any(), any())(any()))
        .thenReturn(Future.successful(identificationTypes))

      val userAnswers = emptyUserAnswers
        .setValue(BorderModeOfTransportPage, BorderMode("4", "Air"))

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, identificationRoute)
        .withFormUrlEncodedBody(("value", identificationType1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      when(mockMeansOfTransportIdentificationTypesActiveService.getMeansOfTransportIdentificationTypesActive(any(), any())(any()))
        .thenReturn(Future.successful(identificationTypes))

      val userAnswers = emptyUserAnswers
        .setValue(BorderModeOfTransportPage, BorderMode("4", "Air"))
      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, identificationRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[IdentificationView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, identificationTypes, mode, activeIndex)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, identificationRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, identificationRoute)
        .withFormUrlEncodedBody(("value", identificationType1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
