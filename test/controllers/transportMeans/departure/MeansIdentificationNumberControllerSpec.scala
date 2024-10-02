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
import forms.DepartureTransportMeansIdentificationNumberFormProvider
import generators.Generators
import models.NormalMode
import navigation.TransportMeansDepartureNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.departure.MeansIdentificationNumberPage
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.transportMeans.departure.MeansIdentificationNumberViewModel
import viewModels.transportMeans.departure.MeansIdentificationNumberViewModel.MeansIdentificationNumberViewModelProvider
import views.html.transportMeans.departure.MeansIdentificationNumberView

class MeansIdentificationNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val prefix: String = "transportMeans.departure.meansIdentificationNumber"

  private def form: Form[String] = app.injector.instanceOf[DepartureTransportMeansIdentificationNumberFormProvider].apply(prefix)

  private val mode                                = NormalMode
  private lazy val meansIdentificationNumberRoute = routes.MeansIdentificationNumberController.onPageLoad(lrn, mode, departureIndex).url

  private lazy val mockViewModelProvider = mock[MeansIdentificationNumberViewModelProvider]

  private val validAnswer = "teststring"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportMeansDepartureNavigatorProvider]).toInstance(fakeTransportMeansDepartureNavigatorProvider))
      .overrides(bind(classOf[MeansIdentificationNumberViewModelProvider]).toInstance(mockViewModelProvider))

  private val viewModel = arbitrary[MeansIdentificationNumberViewModel].sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
    when(mockViewModelProvider.apply(any(), any())).thenReturn(viewModel)
  }

  "MeansIdentificationNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, meansIdentificationNumberRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[MeansIdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, mode, viewModel, departureIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(MeansIdentificationNumberPage(departureIndex), validAnswer)

      setExistingUserAnswers(userAnswers)

      val request    = FakeRequest(GET, meansIdentificationNumberRoute)
      val filledForm = form.bind(Map("value" -> validAnswer))

      val result = route(app, request).value

      val view = injector.instanceOf[MeansIdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, viewModel, departureIndex)(request, messages).toString

    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, meansIdentificationNumberRoute)
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request    = FakeRequest(POST, meansIdentificationNumberRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> ""))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[MeansIdentificationNumberView]

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, viewModel, departureIndex)(request, messages).toString()
    }

    "must redirect to Session Expired for a GET" - {
      "when no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(GET, meansIdentificationNumberRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }

    }

    "must redirect to Session Expired for a POST" - {
      "when no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(POST, meansIdentificationNumberRoute)
          .withFormUrlEncodedBody(("value", validAnswer))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }
    }
  }
}
