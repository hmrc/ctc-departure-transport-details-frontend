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

package controllers.carrierDetails.contact

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.TelephoneNumberFormProvider
import generators.Generators
import models.NormalMode
import navigation.TransportNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.carrierDetails.contact._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.carrierDetails.contact.TelephoneNumberView

import scala.concurrent.Future

class TelephoneNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider              = new TelephoneNumberFormProvider()
  private val name                      = nonEmptyString.sample.value
  private val form                      = formProvider("carrierDetails.contact.telephoneNumber", name)
  private val mode                      = NormalMode
  private lazy val telephoneNumberRoute = routes.TelephoneNumberController.onPageLoad(lrn, mode).url

  private val validAnswer: String = "+123123"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportNavigatorProvider]).toInstance(fakeTransportNavigatorProvider))

  "TelephoneNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.setValue(NamePage, name)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, telephoneNumberRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[TelephoneNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, mode, name)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .setValue(NamePage, name)
        .setValue(TelephoneNumberPage, validAnswer)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, telephoneNumberRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> validAnswer))

      val view = injector.instanceOf[TelephoneNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, name)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers = emptyUserAnswers.setValue(NamePage, name)
      setExistingUserAnswers(userAnswers)

      when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))

      val request = FakeRequest(POST, telephoneNumberRoute)
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.setValue(NamePage, name)
      setExistingUserAnswers(userAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, telephoneNumberRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[TelephoneNumberView]

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, name)(request, messages).toString
    }

    "must redirect to Session Expired for a GET" - {

      "when no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(GET, telephoneNumberRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }

      "when no contact name is found" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, telephoneNumberRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.technicalDifficultiesUrl
      }
    }

    "must redirect to Session Expired for a POST" - {

      "when no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(POST, telephoneNumberRoute)
          .withFormUrlEncodedBody(("value", validAnswer))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }

      "when no contact name is found" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, telephoneNumberRoute)
          .withFormUrlEncodedBody(("value", validAnswer))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.technicalDifficultiesUrl
      }
    }
  }
}
