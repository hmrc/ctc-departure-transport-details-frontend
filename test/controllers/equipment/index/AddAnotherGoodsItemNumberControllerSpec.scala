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

package controllers.equipment.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.equipment.index.itemNumber.{routes => itemNumberRoutes}
import forms.AddAnotherFormProvider
import generators.Generators
import models.{Index, NormalMode}
import navigation.EquipmentNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.ListItem
import viewModels.equipment.index.AddAnotherGoodsItemNumberViewModel
import viewModels.equipment.index.AddAnotherGoodsItemNumberViewModel.AddAnotherGoodsItemNumberViewModelProvider
import views.html.equipment.index.AddAnotherGoodsItemNumberView

class AddAnotherGoodsItemNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar with Generators {

  private val formProvider = new AddAnotherFormProvider()

  private def form(viewModel: AddAnotherGoodsItemNumberViewModel) =
    formProvider(viewModel.prefix, viewModel.allowMore(frontendAppConfig))

  private val mode = NormalMode

  private lazy val addAnotherGoodsItemNumberRoute = routes.AddAnotherGoodsItemNumberController.onPageLoad(lrn, mode, equipmentIndex).url

  private val mockViewModelProvider = mock[AddAnotherGoodsItemNumberViewModelProvider]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[AddAnotherGoodsItemNumberViewModelProvider]).toInstance(mockViewModelProvider))
      .overrides(bind(classOf[EquipmentNavigatorProvider]).toInstance(fakeEquipmentNavigatorProvider))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
  }

  private val listItem          = arbitrary[ListItem].sample.value
  private val listItems         = Seq.fill(Gen.choose(1, frontendAppConfig.maxGoodsItemNumbers - 1).sample.value)(listItem)
  private val maxedOutListItems = Seq.fill(frontendAppConfig.maxGoodsItemNumbers)(listItem)

  private val viewModel = arbitrary[AddAnotherGoodsItemNumberViewModel].sample.value

  private val emptyViewModel       = viewModel.copy(listItems = Nil)
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  "AddAnotherGoodsItemNumber Controller" - {

    "redirect to add goodsItemNumber yes/no page" - {
      "when 0 goodsItemNumber" in {
        when(mockViewModelProvider.apply(any(), any(), any())(any()))
          .thenReturn(emptyViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherGoodsItemNumberRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.AddGoodsItemNumberYesNoController.onPageLoad(lrn, mode, equipmentIndex).url
      }
    }

    "must return OK and the correct view for a GET" - {
      "when max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherGoodsItemNumberRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherGoodsItemNumberView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(notMaxedOutViewModel), lrn, notMaxedOutViewModel)(
            request,
            messages,
            frontendAppConfig
          ).toString
      }

      "when max limit reached" in {
        when(mockViewModelProvider.apply(any(), any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, addAnotherGoodsItemNumberRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherGoodsItemNumberView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form(maxedOutViewModel), lrn, maxedOutViewModel)(
            request,
            messages,
            frontendAppConfig
          ).toString
      }
    }

    "when max limit not reached" - {
      "when yes submitted" - {
        "must redirect to goods item number page at next index" in {
          when(mockViewModelProvider.apply(any(), any(), any())(any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherGoodsItemNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            itemNumberRoutes.ItemNumberController.onPageLoad(lrn, mode, equipmentIndex, Index(listItems.length)).url
        }
      }

      "when no submitted" - {
        "must redirect to next page" in {
          when(mockViewModelProvider.apply(any(), any(), any())(any()))
            .thenReturn(notMaxedOutViewModel)

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, addAnotherGoodsItemNumberRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }
    }

    "when max limit reached" - {
      "must redirect to next page" in {
        when(mockViewModelProvider.apply(any(), any(), any())(any()))
          .thenReturn(maxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherGoodsItemNumberRoute)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors" - {
      "when invalid data is submitted and max limit not reached" in {
        when(mockViewModelProvider.apply(any(), any(), any())(any()))
          .thenReturn(notMaxedOutViewModel)

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, addAnotherGoodsItemNumberRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form(notMaxedOutViewModel).bind(Map("value" -> ""))

        val result = route(app, request).value

        val view = injector.instanceOf[AddAnotherGoodsItemNumberView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, lrn, notMaxedOutViewModel)(request, messages, frontendAppConfig).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, addAnotherGoodsItemNumberRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(POST, addAnotherGoodsItemNumberRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
