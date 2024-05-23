/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.additionalInformation.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.SelectableFormProvider
import generators.Generators
import models.{NormalMode, SelectableList}
import navigation.AdditionalInformationNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.additionalInformation.index.AdditionalInformationTypePage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AdditionalInformationService
import views.html.additionalInformation.index.AdditionalInformationTypeView

import scala.concurrent.Future

class AdditionalInformationTypeControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val additionalInformationCode1    = arbitraryAdditionalInformationCode.arbitrary.sample.get
  private val additionalInformationCode2    = arbitraryAdditionalInformationCode.arbitrary.sample.get
  private val additionalInformationCodeList = SelectableList(Seq(additionalInformationCode1, additionalInformationCode2))

  private val formProvider = new SelectableFormProvider()
  private val form         = formProvider("additionalInformation.index.additionalInformationType", additionalInformationCodeList)
  private val mode         = NormalMode

  private val mockAdditionalInformationService: AdditionalInformationService = mock[AdditionalInformationService]
  private lazy val additionalInformationTypeRoute                            = routes.AdditionalInformationTypeController.onPageLoad(additionalInformationIndex, lrn, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[AdditionalInformationNavigatorProvider]).toInstance(fakeAdditionalInformationNavigatorProvider))
      .overrides(bind(classOf[AdditionalInformationService]).toInstance(mockAdditionalInformationService))

  "AdditionalInformationType Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockAdditionalInformationService.getAdditionalInformationCodes(any())(any())).thenReturn(Future.successful(additionalInformationCodeList))
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, additionalInformationTypeRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[AdditionalInformationTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, additionalInformationCodeList.values, mode, additionalInformationIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockAdditionalInformationService.getAdditionalInformationCodes(any())(any())).thenReturn(Future.successful(additionalInformationCodeList))
      val userAnswers = emptyUserAnswers.setValue(AdditionalInformationTypePage(additionalInformationIndex), additionalInformationCode1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, additionalInformationTypeRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> additionalInformationCode1.value))

      val view = injector.instanceOf[AdditionalInformationTypeView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, additionalInformationCodeList.values, mode, additionalInformationIndex)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockAdditionalInformationService.getAdditionalInformationCodes(any())(any())).thenReturn(Future.successful(additionalInformationCodeList))
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, additionalInformationTypeRoute)
        .withFormUrlEncodedBody(("value", additionalInformationCode1.value))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockAdditionalInformationService.getAdditionalInformationCodes(any())(any())).thenReturn(Future.successful(additionalInformationCodeList))
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, additionalInformationTypeRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[AdditionalInformationTypeView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, additionalInformationCodeList.values, mode, additionalInformationIndex)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, additionalInformationTypeRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, additionalInformationTypeRoute)
        .withFormUrlEncodedBody(("value", additionalInformationCode1.value))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
