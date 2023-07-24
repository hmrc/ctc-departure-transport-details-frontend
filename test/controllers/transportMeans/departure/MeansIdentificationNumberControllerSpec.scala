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
import forms.IdentificationNumberFormProvider
import generators.Generators
import models.NormalMode
import models.transportMeans.InlandMode
import models.transportMeans.departure.Identification
import models.transportMeans.departure.Identification._
import navigation.TransportMeansNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import pages.transportMeans.InlandModePage
import pages.transportMeans.departure.{IdentificationPage, MeansIdentificationNumberPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transportMeans.departure.MeansIdentificationNumberView

import scala.concurrent.Future

class MeansIdentificationNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val identification: Identification = arbitrary[Identification].sample.value
  private val inlandMode: InlandMode         = arbitrary[InlandMode].sample.value

  private val withNoIDTypePrefix: String = "transportMeans.departure.meansIdentificationNumber.withNoIDType"
  private val withIDTypePrefix: String   = "transportMeans.departure.meansIdentificationNumber.withIDType"

  private def withNoIDTypeForm: Form[String] = app.injector.instanceOf[IdentificationNumberFormProvider].apply(withNoIDTypePrefix)

  private def withIDTypeForm(identification: Identification): Form[String] =
    app.injector.instanceOf[IdentificationNumberFormProvider].apply(withIDTypePrefix, identification.arg)

  private val mode                                = NormalMode
  private lazy val meansIdentificationNumberRoute = routes.MeansIdentificationNumberController.onPageLoad(lrn, mode).url

  private val validAnswer = "teststring"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportMeansNavigatorProvider]).toInstance(fakeTransportMeansNavigatorProvider))

  "MeansIdentificationNumber Controller" - {

    "must return OK and the correct view for a GET" - {
      "when Identification Type page has been answered" in {

        val userAnswers = emptyUserAnswers
          .setValue(InlandModePage, inlandMode)
          .setValue(IdentificationPage, identification)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, meansIdentificationNumberRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[MeansIdentificationNumberView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(withIDTypeForm(identification), lrn, mode, withIDTypePrefix, identification.arg)(request, messages).toString
      }

      "when Identification Type page has not been answered" in {

        val userAnswers = emptyUserAnswers
          .setValue(InlandModePage, inlandMode)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, meansIdentificationNumberRoute)

        val result = route(app, request).value

        val view = injector.instanceOf[MeansIdentificationNumberView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(withNoIDTypeForm, lrn, mode, withNoIDTypePrefix)(request, messages).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" - {
      "when identification type page has been answered" in {
        val userAnswers = emptyUserAnswers
          .setValue(InlandModePage, inlandMode)
          .setValue(IdentificationPage, identification)
          .setValue(MeansIdentificationNumberPage, validAnswer)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, meansIdentificationNumberRoute)

        val result = route(app, request).value

        val filledForm = withIDTypeForm(identification).bind(Map("value" -> validAnswer))

        val view = injector.instanceOf[MeansIdentificationNumberView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, lrn, mode, withIDTypePrefix, identification.arg)(request, messages).toString

      }

      "when identification type page has not been answered" in {
        val userAnswers = emptyUserAnswers
          .setValue(InlandModePage, inlandMode)
          .setValue(MeansIdentificationNumberPage, validAnswer)

        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(GET, meansIdentificationNumberRoute)

        val result = route(app, request).value

        val filledForm = withNoIDTypeForm.bind(Map("value" -> validAnswer))

        val view = injector.instanceOf[MeansIdentificationNumberView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(filledForm, lrn, mode, withNoIDTypePrefix)(request, messages).toString

      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(
        emptyUserAnswers
          .setValue(InlandModePage, inlandMode)
          .setValue(IdentificationPage, identification)
      )

      val request = FakeRequest(POST, meansIdentificationNumberRoute)
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" - {
      "when identification page has been answered" in {

        val userAnswers = emptyUserAnswers
          .setValue(InlandModePage, inlandMode)
          .setValue(IdentificationPage, identification)

        setExistingUserAnswers(userAnswers)

        val invalidAnswer = ""

        val request    = FakeRequest(POST, meansIdentificationNumberRoute).withFormUrlEncodedBody(("value", ""))
        val filledForm = withIDTypeForm(identification).bind(Map("value" -> invalidAnswer))

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST

        val view = injector.instanceOf[MeansIdentificationNumberView]

        contentAsString(result) mustEqual
          view(filledForm, lrn, mode, withIDTypePrefix, identification.arg)(request, messages).toString()
      }

      "when identification page has not been answered" in {

        val userAnswers = emptyUserAnswers
          .setValue(InlandModePage, inlandMode)

        setExistingUserAnswers(userAnswers)

        val invalidAnswer = ""

        val request    = FakeRequest(POST, meansIdentificationNumberRoute).withFormUrlEncodedBody(("value", ""))
        val filledForm = withNoIDTypeForm.bind(Map("value" -> invalidAnswer))

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST

        val view = injector.instanceOf[MeansIdentificationNumberView]

        contentAsString(result) mustEqual
          view(filledForm, lrn, mode, withNoIDTypePrefix)(request, messages).toString()
      }
    }

    "must redirect to Session Expired for a GET" - {

      "when no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(GET, meansIdentificationNumberRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
      }

    }

    "must redirect to Session Expired for a POST" - {

      "when no existing data is found" in {

        setNoExistingUserAnswers()

        val request = FakeRequest(POST, meansIdentificationNumberRoute)
          .withFormUrlEncodedBody(("value", validAnswer))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
      }
    }
  }
}
