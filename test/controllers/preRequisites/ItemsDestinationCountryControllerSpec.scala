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

package controllers.preRequisites

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.SelectableFormProvider.CountryFormProvider
import generators.Generators
import models.{NormalMode, SelectableList, UserAnswers}
import navigation.TransportNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.preRequisites.ItemsDestinationCountryPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.preRequisites.ItemsDestinationCountryView

import scala.concurrent.Future

class ItemsDestinationCountryControllerSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ScalaCheckPropertyChecks {

  private val country1    = arbitraryCountry.arbitrary.sample.get
  private val country2    = arbitraryCountry.arbitrary.sample.get
  private val countryList = SelectableList(Seq(country1, country2))

  private val formProvider = new CountryFormProvider()
  private val field        = formProvider.field
  private val form         = formProvider("preRequisites.itemsDestinationCountry", countryList)
  private val mode         = NormalMode

  private lazy val itemsDestinationCountryRoute = routes.ItemsDestinationCountryController.onPageLoad(lrn, mode).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportNavigatorProvider]).toInstance(fakeTransportNavigatorProvider))

  "ItemsDestinationCountry Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockCountriesService.getCountries()(any())).thenReturn(Future.successful(countryList))
      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, itemsDestinationCountryRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[ItemsDestinationCountryView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, countryList.values, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      when(mockCountriesService.getCountries()(any())).thenReturn(Future.successful(countryList))
      val userAnswers = emptyUserAnswers.setValue(ItemsDestinationCountryPage, country1)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, itemsDestinationCountryRoute)

      val result = route(app, request).value

      val filledForm = form.bind(Map(field -> country1.code.code))

      val view = injector.instanceOf[ItemsDestinationCountryView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, countryList.values, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      forAll(arbitrary[Boolean]) {
        isInCL009 =>
          beforeEach()

          when(mockCountriesService.getCountries()(any())).thenReturn(Future.successful(countryList))
          when(mockCountriesService.isInCL009(any())(any())).thenReturn(Future.successful(isInCL009))
          when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, itemsDestinationCountryRoute)
            .withFormUrlEncodedBody((field, country1.code.code))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url

          val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
          userAnswersCaptor.getValue.data mustEqual Json.parse(s"""
               |{
               |  "transportDetails" : {
               |    "preRequisites" : {
               |      "itemsDestinationCountry" : {
               |        "code" : "${country1.code.code}",
               |        "description" : "${country1.description}",
               |        "isInCL009" : $isInCL009
               |      }
               |    }
               |  }
               |}
               |""".stripMargin)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      when(mockCountriesService.getCountries()(any())).thenReturn(Future.successful(countryList))
      setExistingUserAnswers(emptyUserAnswers)

      val request   = FakeRequest(POST, itemsDestinationCountryRoute).withFormUrlEncodedBody((field, "invalid value"))
      val boundForm = form.bind(Map(field -> "invalid value"))

      val result = route(app, request).value

      val view = injector.instanceOf[ItemsDestinationCountryView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, lrn, countryList.values, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, itemsDestinationCountryRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, itemsDestinationCountryRoute)
        .withFormUrlEncodedBody((field, country1.code.code))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
