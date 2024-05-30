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
import forms.SelectableFormProvider
import generators.Generators
import models.{NormalMode, SelectableList}
import navigation.TransportMeansDepartureNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.transportMeans.departure.VehicleCountryPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.NationalitiesService
import views.html.transportMeans.departure.VehicleCountryView

import scala.concurrent.Future

class VehicleCountryControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  private val nationality1    = arbitraryNationality.arbitrary.sample.get
  private val nationality2    = arbitraryNationality.arbitrary.sample.get
  private val nationalityList = SelectableList(Seq(nationality1, nationality2))

  private val formProvider = new SelectableFormProvider()
  private val form         = formProvider("transportMeans.departure.vehicleCountry", nationalityList)
  private val mode         = NormalMode

  private val mockNationalitiesService: NationalitiesService = mock[NationalitiesService]
  private lazy val vehicleCountryRoute                       = routes.VehicleCountryController.onPageLoad(lrn, mode, departureIndex).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportMeansDepartureNavigatorProvider]).toInstance(fakeTransportMeansDepartureNavigatorProvider))
      .overrides(bind(classOf[NationalitiesService]).toInstance(mockNationalitiesService))

  "VehicleCountry Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockNationalitiesService.getNationalities()(any())).thenReturn(Future.successful(nationalityList))
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, vehicleCountryRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[VehicleCountryView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, nationalityList.values, mode, departureIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockNationalitiesService.getNationalities()(any())).thenReturn(Future.successful(nationalityList))
      val userAnswers = emptyUserAnswers.setValue(VehicleCountryPage(departureIndex), nationality1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, vehicleCountryRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map("value" -> nationality1.code))

      val view = injector.instanceOf[VehicleCountryView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, nationalityList.values, mode, departureIndex)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockNationalitiesService.getNationalities()(any())).thenReturn(Future.successful(nationalityList))
      when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, vehicleCountryRoute)
        .withFormUrlEncodedBody(("value", nationality1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockNationalitiesService.getNationalities()(any())).thenReturn(Future.successful(nationalityList))
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, vehicleCountryRoute).withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[VehicleCountryView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, nationalityList.values, mode, departureIndex)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, vehicleCountryRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, vehicleCountryRoute)
        .withFormUrlEncodedBody(("value", nationality1.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
