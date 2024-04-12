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
import models.reference.transportMeans.departure.Identification
import models.{NormalMode, TransportMeans, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.sections.transportMeans.TransportMeansSection
import pages.transportMeans.departure._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transportMeans.departure.RemoveDepartureMeansOfTransportYesNoView

import scala.concurrent.Future

class RemoveDepartureMeansOfTransportYesNoControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val formProvider = new YesNoFormProvider()
  private val form         = formProvider("transportMeans.departure.removeTransportMeansOfDepartureYesNo")
  private val mode         = NormalMode

  private lazy val removeDepartureTransportMeansRoute =
    controllers.transportMeans.departure.routes.RemoveDepartureMeansOfTransportYesNoController.onPageLoad(lrn, mode, index).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()

  "RemoveBorderTransportYesNoController Controller" - {

    "must return OK and the correct view for a GET" in {

      forAll(arbitrary[Identification].sample.value, nonEmptyString) {
        (identifier, identificationNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(IdentificationPage(index), identifier)
            .setValue(MeansIdentificationNumberPage(index), identificationNumber)

          val insetText = TransportMeans(index, Some(identifier), Some(identificationNumber)).forRemoveDisplay

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, removeDepartureTransportMeansRoute)

          val result = route(app, request).value

          val view = injector.instanceOf[RemoveDepartureMeansOfTransportYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, lrn, mode, index, insetText)(request, messages).toString
      }
    }

    "when yes submitted" - {
      "must redirect to add another departureTransportMeans and remove departureTransportMeans at specified index" in {
        reset(mockSessionRepository)
        when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)
        val userAnswers = emptyUserAnswers.setValue(TransportMeansSection, Json.obj())

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeDepartureTransportMeansRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.AddAnotherDepartureTransportMeansController.onPageLoad(lrn, mode).url

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
        userAnswersCaptor.getValue.get(TransportMeansSection) mustNot be(defined)
      }
    }

    "when no submitted" - {
      "must redirect to add another departureTransportMeans and not remove departureTransportMeans at specified index" in {
        reset(mockSessionRepository)
        when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)
        val userAnswers = emptyUserAnswers.setValue(TransportMeansSection, Json.obj())

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, removeDepartureTransportMeansRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.AddAnotherDepartureTransportMeansController.onPageLoad(lrn, mode).url

        verify(mockSessionRepository, never()).set(any())(any())

      }
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers.setValue(TransportMeansSection, Json.obj()))

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      val request = FakeRequest(POST, removeDepartureTransportMeansRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.AddAnotherDepartureTransportMeansController.onPageLoad(lrn, mode).url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      forAll(arbitrary[Identification], nonEmptyString) {
        (identifier, identificationNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(IdentificationPage(index), identifier)
            .setValue(MeansIdentificationNumberPage(index), identificationNumber)

          val insetText = TransportMeans(index, Some(identifier), Some(identificationNumber)).forRemoveDisplay

          setExistingUserAnswers(userAnswers)

          val invalidAnswer = ""

          val request    = FakeRequest(POST, removeDepartureTransportMeansRoute).withFormUrlEncodedBody(("value", ""))
          val filledForm = form.bind(Map("value" -> invalidAnswer))

          val result = route(app, request).value

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[RemoveDepartureMeansOfTransportYesNoView]

          contentAsString(result) mustEqual
            view(filledForm, lrn, mode, index, insetText)(request, messages).toString
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, removeDepartureTransportMeansRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, removeDepartureTransportMeansRoute)
        .withFormUrlEncodedBody(("value", "test string"))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
