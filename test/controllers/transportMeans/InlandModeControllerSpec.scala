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

package controllers.transportMeans

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.EnumerableFormProvider
import generators.Generators
import models.NormalMode
import models.reference.InlandMode
import navigation.TransportNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.transportMeans.InlandModePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TransportModeCodesService
import views.html.transportMeans.InlandModeView

import scala.concurrent.Future

class InlandModeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val inlandModes = arbitrary[Seq[InlandMode]].sample.value

  private val formProvider         = new EnumerableFormProvider()
  private val form                 = formProvider[InlandMode]("transportMeans.inlandMode", inlandModes)
  private val mode                 = NormalMode
  private lazy val inlandModeRoute = routes.InlandModeController.onPageLoad(lrn, mode).url

  private val mockTransportModeCodesService: TransportModeCodesService = mock[TransportModeCodesService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportNavigatorProvider]).toInstance(fakeTransportNavigatorProvider))
      .overrides(bind(classOf[TransportModeCodesService]).toInstance(mockTransportModeCodesService))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockTransportModeCodesService)
    when(mockTransportModeCodesService.getInlandModes()(any())).thenReturn(Future.successful(inlandModes))
  }

  "InlandMode Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, inlandModeRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[InlandModeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, inlandModes, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(InlandModePage, inlandModes.head)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, inlandModeRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> inlandModes.head.code))

      val view = injector.instanceOf[InlandModeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, inlandModes, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, inlandModeRoute)
        .withFormUrlEncodedBody(("value", inlandModes.head.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, inlandModeRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[InlandModeView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, inlandModes, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, inlandModeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, inlandModeRoute)
        .withFormUrlEncodedBody(("value", inlandModes.head.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
