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
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.InlandModePage
import pages.transportMeans.active.InferredIdentificationPage
import pages.transportMeans.departure.{IdentificationPage, MeansIdentificationNumberPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transportMeans.departure.MeansIdentificationNumberView

import scala.concurrent.Future

class MeansIdentificationNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val identification: Identification = arbitrary[Identification].sample.value
  private val inlandMode: InlandMode         = arbitrary[InlandMode].sample.value

  private val prefix: String = "transportMeans.departure.meansIdentificationNumber"

  private def form: Form[String] = app.injector.instanceOf[IdentificationNumberFormProvider].apply(prefix)

  private val mode                                = NormalMode
  private lazy val meansIdentificationNumberRoute = routes.MeansIdentificationNumberController.onPageLoad(lrn, mode).url
  private val identifierPageGen                   = Gen.oneOf(IdentificationPage, InferredIdentificationPage(activeIndex))

  private val validAnswer = "teststring"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportMeansNavigatorProvider]).toInstance(fakeTransportMeansNavigatorProvider))

  "MeansIdentificationNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers
        .setValue(InlandModePage, inlandMode)
        .setValue(IdentificationPage, identification)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, meansIdentificationNumberRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[MeansIdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, mode, prefix, identification.arg)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = emptyUserAnswers
        .setValue(InlandModePage, inlandMode)
        .setValue(IdentificationPage, identification)
        .setValue(MeansIdentificationNumberPage, "testString")

      setExistingUserAnswers(userAnswers)

      val request    = FakeRequest(GET, meansIdentificationNumberRoute)
      val filledForm = form.bind(Map("value" -> "testString"))

      val result = route(app, request).value

      val view = injector.instanceOf[MeansIdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, prefix, identification.arg)(request, messages).toString

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

    "must return a Bad Request and errors when invalid data is submitted" in {

      forAll(arbitrary[Identification], identifierPageGen) {
        (identifier, page) =>
          val userAnswers = emptyUserAnswers
            .setValue(InlandModePage, inlandMode)
            .setValue(IdentificationPage, identification)
          setExistingUserAnswers(userAnswers)

          val request    = FakeRequest(POST, meansIdentificationNumberRoute).withFormUrlEncodedBody(("value", ""))
          val filledForm = form.bind(Map("value" -> ""))

          val result = route(app, request).value

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[MeansIdentificationNumberView]

          contentAsString(result) mustEqual
            view(filledForm, lrn, mode, prefix, identification.arg)(request, messages).toString()

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
