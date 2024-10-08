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
import forms.YesNoFormProvider
import generators.Generators
import models.removable.ActiveBorderTransport
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.sections.transportMeans.ActiveSection
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transportMeans.active.ConfirmRemoveBorderTransportView

import scala.concurrent.Future

class ConfirmRemoveBorderTransportControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider                           = new YesNoFormProvider()
  private val form                                   = formProvider("transportMeans.active.confirmRemoveBorderTransport", activeIndex.display)
  private val mode                                   = NormalMode
  private lazy val confirmRemoveBorderTransportRoute = routes.ConfirmRemoveBorderTransportController.onPageLoad(lrn, mode, activeIndex).url

  "ConfirmRemoveBorderTransport Controller" - {

    "must return OK and the correct view for a GET" in {
      forAll(arbitraryTransportMeansActiveAnswers(emptyUserAnswers, activeIndex)) {
        userAnswers =>
          setExistingUserAnswers(userAnswers)

          val request                   = FakeRequest(GET, confirmRemoveBorderTransportRoute)
          val result                    = route(app, request).value
          val insetText: Option[String] = ActiveBorderTransport(userAnswers, activeIndex).map(_.forRemoveDisplay)

          val view = injector.instanceOf[ConfirmRemoveBorderTransportView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, lrn, mode, activeIndex, insetText)(request, messages).toString
      }
    }

    "when yes submitted" - {
      "must redirect to the next page when valid data is submitted and call to remove a transport active border" in {
        forAll(arbitraryTransportMeansActiveAnswers(emptyUserAnswers, activeIndex)) {
          userAnswers =>
            reset(mockSessionRepository)
            when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))

            setExistingUserAnswers(userAnswers)

            val request = FakeRequest(POST, confirmRemoveBorderTransportRoute)
              .withFormUrlEncodedBody(("value", "true"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              routes.AddAnotherBorderTransportController.onPageLoad(userAnswers.lrn, mode).url

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
            userAnswersCaptor.getValue.get(ActiveSection(activeIndex)) mustNot be(defined)
        }
      }
    }

    "when no submitted" - {
      "must redirect to add another supply chain actor and not remove supply chain actor at specified index" in {
        forAll(arbitraryTransportMeansActiveAnswers(emptyUserAnswers, activeIndex)) {
          userAnswers =>
            reset(mockSessionRepository)

            setExistingUserAnswers(userAnswers)

            val request = FakeRequest(POST, confirmRemoveBorderTransportRoute)
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              routes.AddAnotherBorderTransportController.onPageLoad(lrn, mode).url

            verify(mockSessionRepository, never()).set(any())(any())
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      forAll(arbitraryTransportMeansActiveAnswers(emptyUserAnswers, activeIndex)) {
        userAnswers =>
          setExistingUserAnswers(userAnswers)

          val request                   = FakeRequest(POST, confirmRemoveBorderTransportRoute).withFormUrlEncodedBody(("value", ""))
          val boundForm                 = form.bind(Map("value" -> ""))
          val result                    = route(app, request).value
          val insetText: Option[String] = ActiveBorderTransport(userAnswers, activeIndex).map(_.forRemoveDisplay)

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[ConfirmRemoveBorderTransportView]

          contentAsString(result) mustEqual
            view(boundForm, lrn, mode, activeIndex, insetText)(request, messages).toString
      }
    }

    "must redirect to Session Expired for a GET" - {
      "when no existing data found" in {
        setNoExistingUserAnswers()

        val request = FakeRequest(GET, confirmRemoveBorderTransportRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }

      "when no active border transport means found" in {
        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, confirmRemoveBorderTransportRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.AddAnotherBorderTransportController.onPageLoad(lrn, mode).url
      }
    }

    "must redirect to Session Expired for a POST if no existing data is found" - {
      "when no existing data found" in {
        setNoExistingUserAnswers()

        val request = FakeRequest(POST, confirmRemoveBorderTransportRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }

      "when no active border transport means found" in {
        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, confirmRemoveBorderTransportRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.AddAnotherBorderTransportController.onPageLoad(lrn, mode).url
      }
    }
  }
}
