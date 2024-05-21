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

package controllers.additionalInformation.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import generators.Generators
import models.reference.additionalInformation.AdditionalInformationCode
import models.removable.AdditionalInformation
import models.{Index, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.additionalInformation.index.AdditionalInformationTypePage
import pages.sections.additionalInformation.AdditionalInformationSection
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.additionalInformation.RemoveAdditionalInformationYesNoView

import scala.concurrent.Future

class RemoveAdditionalInformationYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar with Generators {

  private val formProvider = new YesNoFormProvider()

  private def form: Form[Boolean] =
    formProvider("additionalInformation.index.removeAdditionalInformationYesNo")

  private val mode                                  = NormalMode
  private lazy val removeAdditionalInformationRoute = routes.RemoveAdditionalInformationYesNoController.onPageLoad(lrn, additionalInformationIndex, mode).url

  val additionalInformationCode: AdditionalInformationCode = arbitrary[AdditionalInformationCode].sample.value
  val additionalInformation: AdditionalInformation         = AdditionalInformation(additionalInformationCode)

  "RemoveAdditionalInformationYesNoControllerSpec" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers
        .setValue(AdditionalInformationTypePage(additionalInformationIndex), additionalInformationCode)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, removeAdditionalInformationRoute)
      val result  = route(app, request).value

      val view = injector.instanceOf[RemoveAdditionalInformationYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, additionalInformationIndex, Some(additionalInformationCode.toString), mode)(request, messages).toString
    }

    "when yes submitted and at least 1 item remains must redirect to add another additional information and remove additional information at specified index" in {
      reset(mockSessionRepository)
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .setValue(AdditionalInformationTypePage(additionalInformationIndex), additionalInformationCode)
        .setValue(AdditionalInformationTypePage(Index(1)), additionalInformationCode)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, removeAdditionalInformationRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.additionalInformation.routes.AddAnotherAdditionalInformationController.onPageLoad(lrn, mode).url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
      userAnswersCaptor.getValue.get(AdditionalInformationSection(index)) must be(defined)
    }

    "when yes submitted and no items left must redirect to do you want to add additional information and remove additional information at specified index" in {
      reset(mockSessionRepository)
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .setValue(AdditionalInformationTypePage(additionalInformationIndex), additionalInformationCode)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, removeAdditionalInformationRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.additionalInformation.routes.AddAdditionalInformationYesNoController.onPageLoad(lrn, mode).url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
      userAnswersCaptor.getValue.get(AdditionalInformationSection(index)) mustNot be(defined)
    }

    "when no submitted must redirect to add another additional information and not remove the additional information at specified index" in {
      reset(mockSessionRepository)
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .setValue(AdditionalInformationTypePage(additionalInformationIndex), additionalInformationCode)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, removeAdditionalInformationRoute)
        .withFormUrlEncodedBody(("value", "false"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.additionalInformation.routes.AddAnotherAdditionalInformationController.onPageLoad(lrn, mode).url

      verify(mockSessionRepository, never()).set(any())(any())
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .setValue(AdditionalInformationTypePage(additionalInformationIndex), additionalInformationCode)

      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, removeAdditionalInformationRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[RemoveAdditionalInformationYesNoView]

      contentAsString(result) mustEqual
        view(boundForm, lrn, additionalInformationIndex, Some(additionalInformationCode.toString), mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET" - {
      "if no existing data found" in {
        setNoExistingUserAnswers()

        val request = FakeRequest(GET, removeAdditionalInformationRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }
    }

    "must redirect to Session Expired for a POST" - {
      "if no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(POST, removeAdditionalInformationRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }

      "if no additional information is found" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, removeAdditionalInformationRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.additionalInformation.routes.AddAnotherAdditionalInformationController.onPageLoad(lrn, mode).url
      }
    }
  }
}
