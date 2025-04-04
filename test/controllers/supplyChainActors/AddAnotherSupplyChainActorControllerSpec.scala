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

package controllers.supplyChainActors

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.AddAnotherFormProvider
import generators.Generators
import models.NormalMode
import navigation.TransportNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import pages.supplyChainActors.AddAnotherSupplyChainActorPage
import viewModels.ListItem
import viewModels.supplyChainActors.AddAnotherSupplyChainActorViewModel
import viewModels.supplyChainActors.AddAnotherSupplyChainActorViewModel.AddAnotherSupplyChainActorViewModelProvider
import views.html.supplyChainActors.AddAnotherSupplyChainActorView

class AddAnotherSupplyChainActorControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar with Generators {

  private val formProvider                         = new AddAnotherFormProvider()
  private val mode                                 = NormalMode
  private lazy val addAnotherSupplyChainActorRoute = routes.AddAnotherSupplyChainActorController.onPageLoad(lrn, mode).url

  private def form(viewModel: AddAnotherSupplyChainActorViewModel) =
    formProvider(viewModel.prefix, viewModel.allowMore(frontendAppConfig))

  private val mockViewModelProvider = mock[AddAnotherSupplyChainActorViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportNavigatorProvider]).toInstance(fakeTransportNavigatorProvider))
      .overrides(bind(classOf[AddAnotherSupplyChainActorViewModelProvider]).toInstance(mockViewModelProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
  }

  private val listItem          = arbitrary[ListItem].sample.value
  private val listItems         = Seq.fill(Gen.choose(1, frontendAppConfig.maxSupplyChainActors - 1).sample.value)(listItem)
  private val maxedOutListItems = Seq.fill(frontendAppConfig.maxSupplyChainActors)(listItem)

  private val viewModel = arbitrary[AddAnotherSupplyChainActorViewModel].sample.value

  private val viewModelWithNoItems = viewModel.copy(listItems = Nil)
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  "AddAnotherSupplyChainActor Controller" - {

    "onPageLoad" - {

      "must populate the view correctly on a GET when the question has previously been answered" - {
        "when max limit not reached" in {
          when(mockViewModelProvider.apply(any(), any())(any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers.setValue(AddAnotherSupplyChainActorPage, true))

          val request = FakeRequest(GET, addAnotherSupplyChainActorRoute)

          val result = route(app, request).value

          val filledForm = form(notMaxedOutViewModel).bind(Map("value" -> "true"))

          val view = injector.instanceOf[AddAnotherSupplyChainActorView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(filledForm, lrn, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
        }

        "when max limit reached" in {
          when(mockViewModelProvider.apply(any(), any())(any()))
            .thenReturn(maxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers.setValue(AddAnotherSupplyChainActorPage, true))

          val request = FakeRequest(GET, addAnotherSupplyChainActorRoute)

          val result = route(app, request).value

          val filledForm = form(maxedOutViewModel).bind(Map("value" -> "true"))

          val view = injector.instanceOf[AddAnotherSupplyChainActorView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(filledForm, lrn, maxedOutViewModel)(request, messages, frontendAppConfig).toString
        }
      }

      "when no supply chain actors" - {
        "must redirect to SupplyChainActorYesNoController" in {
          when(mockViewModelProvider.apply(any(), any())(any()))
            .thenReturn(viewModelWithNoItems)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(GET, addAnotherSupplyChainActorRoute)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.supplyChainActors.routes.SupplyChainActorYesNoController.onPageLoad(lrn, mode).url
        }
      }

      "when max number of supply chain actors" - {
        "must return OK and the correct view" in {

          when(mockViewModelProvider.apply(any(), any())(any()))
            .thenReturn(maxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(GET, addAnotherSupplyChainActorRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[AddAnotherSupplyChainActorView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form(maxedOutViewModel), lrn, maxedOutViewModel)(request, messages, frontendAppConfig).toString
        }
      }

      "when less than max number of supply chain actors" - {
        "must return OK and the correct view" in {

          when(mockViewModelProvider.apply(any(), any())(any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(GET, addAnotherSupplyChainActorRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[AddAnotherSupplyChainActorView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form(notMaxedOutViewModel), lrn, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
        }
      }

      "when errors" - {

        "must redirect to session expired when data is missing" in {

          setNoExistingUserAnswers()

          val request = FakeRequest(GET, addAnotherSupplyChainActorRoute)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
        }
      }
    }

    "onSubmit" - {

      "when max number of supply chain actors" - {
        "must redirect to authorisations section" in {

          when(mockViewModelProvider.apply(any(), any())(any()))
            .thenReturn(maxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherSupplyChainActorRoute)
            .withFormUrlEncodedBody(("value", ""))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url

        }
      }

      "when less than max number of supply chain actors" - {
        "and user selects Yes" - {
          "must redirect to supply chain actor type with next index" in {

            when(mockViewModelProvider.apply(any(), any())(any()))
              .thenReturn(notMaxedOutViewModel)

            setExistingUserAnswers(emptyUserAnswers)

            val request = FakeRequest(POST, addAnotherSupplyChainActorRoute)
              .withFormUrlEncodedBody(("value", "true"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.supplyChainActors.index.routes.SupplyChainActorTypeController
                .onPageLoad(lrn, mode, notMaxedOutViewModel.nextIndex)
                .url

          }
        }

        "and user selects No" - {
          "must redirect to authorisations section" in {

            when(mockViewModelProvider.apply(any(), any())(any()))
              .thenReturn(notMaxedOutViewModel)

            setExistingUserAnswers(emptyUserAnswers)

            val request = FakeRequest(POST, addAnotherSupplyChainActorRoute)
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual onwardRoute.url

          }
        }
      }

      "when errors" - {

        "must return bad request when invalid data is submitted" in {

          when(mockViewModelProvider.apply(any(), any())(any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherSupplyChainActorRoute)
            .withFormUrlEncodedBody(("value", ""))

          val boundForm = form(notMaxedOutViewModel).bind(Map("value" -> ""))

          val result = route(app, request).value

          val view = injector.instanceOf[AddAnotherSupplyChainActorView]

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(boundForm, lrn, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
        }

        "must redirect to session expired when no data is found" in {

          setNoExistingUserAnswers()

          val request = FakeRequest(POST, addAnotherSupplyChainActorRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
        }
      }
    }
  }
}
