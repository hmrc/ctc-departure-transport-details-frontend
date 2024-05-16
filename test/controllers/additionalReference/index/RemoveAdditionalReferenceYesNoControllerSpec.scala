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

package controllers.additionalReference.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.YesNoFormProvider
import generators.Generators
import models.reference.additionalReference.AdditionalReferenceType
import models.removable.AdditionalReference
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.additionalReference.index.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import pages.sections.additionalReference.AdditionalReferenceSection
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.additionalReference.index.RemoveAdditionalReferenceYesNoView

import scala.concurrent.Future

class RemoveAdditionalReferenceYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar with Generators {

  private val formProvider = new YesNoFormProvider()

  private def form: Form[Boolean] =
    formProvider("additionalReference.index.additionalReferenceNumber")

  private val mode                                = NormalMode
  private lazy val removeAdditionalReferenceRoute = routes.RemoveAdditionalReferenceYesNoController.onPageLoad(lrn, mode, additionalReferenceIndex).url

  val additionalReferenceType: AdditionalReferenceType = arbitrary[AdditionalReferenceType].sample.value
  val additionalReferenceNumber: String                = Gen.alphaStr.sample.value

  val additionalReference: AdditionalReference = AdditionalReference(additionalReferenceType, Some(additionalReferenceNumber))

  "RemoveAdditionalReferenceYesNoControllerSpec" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers
        .setValue(AdditionalReferenceTypePage(additionalReferenceIndex), additionalReferenceType)
        .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), additionalReferenceNumber)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, removeAdditionalReferenceRoute)
      val result  = route(app, request).value

      val view = injector.instanceOf[RemoveAdditionalReferenceYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, mode, additionalReferenceIndex, Some(additionalReference.forRemoveDisplay))(request, messages).toString
    }

    "when yes submitted must redirect to add another additional reference and remove additional reference at specified index" in {
      reset(mockSessionRepository)
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .setValue(AdditionalReferenceTypePage(additionalReferenceIndex), additionalReferenceType)
        .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), additionalReferenceNumber)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, removeAdditionalReferenceRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.additionalReference.routes.AddAnotherAdditionalReferenceController.onPageLoad(lrn, mode).url

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
      userAnswersCaptor.getValue.get(AdditionalReferenceSection(index)) mustNot be(defined)
    }

    "when no submitted must redirect to add another additional reference and not remove the additional reference at specified index" in {
      reset(mockSessionRepository)
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      val userAnswers = emptyUserAnswers
        .setValue(AdditionalReferenceTypePage(additionalReferenceIndex), additionalReferenceType)
        .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), additionalReferenceNumber)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, removeAdditionalReferenceRoute)
        .withFormUrlEncodedBody(("value", "false"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.additionalReference.routes.AddAnotherAdditionalReferenceController.onPageLoad(lrn, mode).url

      verify(mockSessionRepository, never()).set(any())(any())
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers
        .setValue(AdditionalReferenceTypePage(additionalReferenceIndex), additionalReferenceType)
        .setValue(AdditionalReferenceNumberPage(additionalReferenceIndex), additionalReferenceNumber)

      setExistingUserAnswers(userAnswers)

      val request   = FakeRequest(POST, removeAdditionalReferenceRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[RemoveAdditionalReferenceYesNoView]

      contentAsString(result) mustEqual
        view(boundForm, lrn, mode, additionalReferenceIndex, Some(additionalReference.forRemoveDisplay))(request, messages).toString
    }

    "must redirect to Session Expired for a GET" - {
      "if no existing data found" in {
        setNoExistingUserAnswers()

        val request = FakeRequest(GET, removeAdditionalReferenceRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }

      "if no additional reference is found" in {
        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, removeAdditionalReferenceRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.additionalReference.routes.AddAnotherAdditionalReferenceController.onPageLoad(lrn, mode).url
      }
    }

    "must redirect to Session Expired for a POST" - {
      "if no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(POST, removeAdditionalReferenceRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }

      "if no additional reference is found" in {

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, removeAdditionalReferenceRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.additionalReference.routes.AddAnotherAdditionalReferenceController.onPageLoad(lrn, mode).url
      }
    }
  }
}
