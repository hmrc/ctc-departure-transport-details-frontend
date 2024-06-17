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

package controllers.transportMeans.departure

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import generators.Generators
import models.NormalMode
import navigation.TransportMeansNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.transportMeans.AddInlandModeYesNoPage
import pages.transportMeans.departure.AddIdentificationTypeYesNoPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.transportMeans.departure.AddIdentificationTypeViewModel
import views.html.transportMeans.departure.AddIdentificationTypeYesNoView

import scala.concurrent.Future

class AddIdentificationTypeYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar with Generators {

  private val formProvider                         = new YesNoFormProvider()
  private val form                                 = formProvider("transportMeans.departure.addIdentificationTypeYesNo")
  private val mode                                 = NormalMode
  private lazy val addIdentificationTypeYesNoRoute = routes.AddIdentificationTypeYesNoController.onPageLoad(lrn, mode, departureIndex).url
  private val addInlandModeYesNo                   = arbitrary[Boolean].sample.value

  private val viewModel = AddIdentificationTypeViewModel(addInlandModeYesNo)

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportMeansNavigatorProvider]).toInstance(fakeTransportMeansNavigatorProvider))

  "AddIdentificationTypeYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers
        .setValue(AddInlandModeYesNoPage, addInlandModeYesNo)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, addIdentificationTypeYesNoRoute)
      val result  = route(app, request).value

      val view = injector.instanceOf[AddIdentificationTypeYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, mode, departureIndex, viewModel)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .setValue(AddIdentificationTypeYesNoPage(departureIndex), true)
        .setValue(AddInlandModeYesNoPage, addInlandModeYesNo)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, addIdentificationTypeYesNoRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> "true"))

      val view = injector.instanceOf[AddIdentificationTypeYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, departureIndex, viewModel)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .setValue(AddInlandModeYesNoPage, addInlandModeYesNo)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, addIdentificationTypeYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .setValue(AddInlandModeYesNoPage, addInlandModeYesNo)

      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, addIdentificationTypeYesNoRoute).withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[AddIdentificationTypeYesNoView]

      contentAsString(result) mustEqual
        view(boundForm, lrn, mode, departureIndex, viewModel)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addIdentificationTypeYesNoRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addIdentificationTypeYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
