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
import config.FrontendAppConfig
import forms.AddAnotherFormProvider
import generators.Generators
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import pages.transportMeans.AddAnotherBorderTransportPage
import viewModels.ListItem
import viewModels.transportMeans.active.AddAnotherBorderTransportViewModel
import viewModels.transportMeans.active.AddAnotherBorderTransportViewModel.AddAnotherBorderTransportViewModelProvider
import views.html.transportMeans.active.AddAnotherBorderTransportView

class AddAnotherBorderTransportControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val formProvider = new AddAnotherFormProvider()

  implicit override def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  private def form(viewModel: AddAnotherBorderTransportViewModel) =
    formProvider(viewModel.prefix, viewModel.allowMore)

  private val mode                                = NormalMode
  private lazy val addAnotherBorderTransportRoute = routes.AddAnotherBorderTransportController.onPageLoad(lrn, mode).url

  private val mockViewModelProvider = mock[AddAnotherBorderTransportViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[AddAnotherBorderTransportViewModelProvider]).toInstance(mockViewModelProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
  }

  private val listItem          = arbitrary[ListItem].sample.value
  private val listItems         = Seq.fill(Gen.choose(1, frontendAppConfig.maxActiveBorderTransports - 1).sample.value)(listItem)
  private val maxedOutListItems = Seq.fill(frontendAppConfig.maxActiveBorderTransports)(listItem)

  private val viewModel = arbitrary[AddAnotherBorderTransportViewModel].sample.value

  private val viewModelWithNoItems = viewModel.copy(listItems = Nil)
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  "AddAnotherBorderTransport Controller" - {

    "redirect to border mode of transport page" - {
      "when 0 active border transports" in {
        when(mockViewModelProvider.apply(any(), any())(any()))
          .thenReturn(viewModelWithNoItems)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherBorderTransportRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.transportMeans.routes.BorderModeOfTransportController.onPageLoad(lrn, mode).url
      }
    }

    "must return OK and the correct view for a GET" - {
      "when max limit not reached" in {

        when(mockViewModelProvider.apply(any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherBorderTransportRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherBorderTransportView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(notMaxedOutViewModel), lrn, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {

        when(mockViewModelProvider.apply(any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherBorderTransportRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherBorderTransportView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(maxedOutViewModel), lrn, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" - {
      "when max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers.setValue(AddAnotherBorderTransportPage, true))

        val request = FakeRequest(GET, addAnotherBorderTransportRoute)

        val result = route(app, request).value

        val filledForm = form(notMaxedOutViewModel).bind(Map("value" -> "true"))

        val view = injector.instanceOf[AddAnotherBorderTransportView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, lrn, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }

      "when max limit reached" in {
        when(mockViewModelProvider.apply(any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers.setValue(AddAnotherBorderTransportPage, true))

        val request = FakeRequest(GET, addAnotherBorderTransportRoute)

        val result = route(app, request).value

        val filledForm = form(maxedOutViewModel).bind(Map("value" -> "true"))

        val view = injector.instanceOf[AddAnotherBorderTransportView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, lrn, maxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "when max limit not reached" - {
      "when yes submitted" - {
        "must redirect to identification type page at next index" in {
          when(mockViewModelProvider.apply(any(), any())(any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherBorderTransportRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.IdentificationController.onPageLoad(lrn, mode, notMaxedOutViewModel.nextIndex).url
        }
      }

      "when no submitted" - {
        "must redirect to CYA" in {
          when(mockViewModelProvider.apply(any(), any())(any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherBorderTransportRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.transportMeans.routes.TransportMeansCheckYourAnswersController.onPageLoad(lrn, mode).url
        }
      }
    }

    "when max limit reached" - {
      "must redirect to CYA" in {
        when(mockViewModelProvider.apply(any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherBorderTransportRoute)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.transportMeans.routes.TransportMeansCheckYourAnswersController.onPageLoad(lrn, mode).url
      }
    }

    "must return a Bad Request and errors" - {
      "when invalid data is submitted and max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherBorderTransportRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form(notMaxedOutViewModel).bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherBorderTransportView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, lrn, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addAnotherBorderTransportRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addAnotherBorderTransportRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
