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
import config.Constants.ModeOfTransport.Road
import forms.EnumerableFormProvider
import generators.Generators
import models.NormalMode
import models.reference.InlandMode
import models.reference.transportMeans.departure.Identification
import navigation.TransportMeansNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.transportMeans.InlandModePage
import pages.transportMeans.departure.IdentificationPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.MeansOfTransportIdentificationTypesService
import viewModels.transportMeans.departure.IdentificationViewModel
import views.html.transportMeans.departure.IdentificationView

import scala.concurrent.Future

class IdentificationControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val identificationTypes = arbitrary[Seq[Identification]].sample.value
  private val identification      = identificationTypes.head

  private val formProvider             = new EnumerableFormProvider()
  private val form                     = formProvider[Identification]("transportMeans.departure.identification", identificationTypes)
  private val mode                     = NormalMode
  private lazy val identificationRoute = routes.IdentificationController.onPageLoad(lrn, mode, departureIndex).url
  private val identificationViewModel  = new IdentificationViewModel(Some(Identification(Road, "test")), departureIndex, Some(InlandMode("code", "desc")))

  private val mockMeansOfIdentificationTypesService = mock[MeansOfTransportIdentificationTypesService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportMeansNavigatorProvider]).toInstance(fakeTransportMeansNavigatorProvider))
      .overrides(bind(classOf[MeansOfTransportIdentificationTypesService]).toInstance(mockMeansOfIdentificationTypesService))

  private val inlandMode = arbitrary[InlandMode].suchThat(_.code != Road).sample.value

  private val baseAnswers = emptyUserAnswers
    .setValue(InlandModePage, inlandMode)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMeansOfIdentificationTypesService)
    when(mockMeansOfIdentificationTypesService.getMeansOfTransportIdentificationTypes(any())(any())).thenReturn(Future.successful(identificationTypes))
  }

  "Identification Controller" - {

    "must return OK and the correct view for a GET " in {

      setExistingUserAnswers(baseAnswers)

      val request = FakeRequest(GET, identificationRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[IdentificationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, identificationTypes, mode, departureIndex, identificationViewModel)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.setValue(IdentificationPage(departureIndex), identification)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, identificationRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> identification.code))

      val view = injector.instanceOf[IdentificationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, identificationTypes, mode, departureIndex, identificationViewModel)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(baseAnswers)

      val request = FakeRequest(POST, identificationRoute)
        .withFormUrlEncodedBody(("value", identification.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted and inlandMode" in {

      setExistingUserAnswers(baseAnswers)

      val request   = FakeRequest(POST, identificationRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[IdentificationView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, identificationTypes, mode, departureIndex, identificationViewModel)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, identificationRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, identificationRoute)
        .withFormUrlEncodedBody(("value", identification.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
