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

package controllers.authorisationsAndLimit.limit

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.DateFormProvider
import models.NormalMode
import models.reference.CustomsOffice
import navigation.TransportNavigatorProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.authorisationsAndLimit.limit.LimitDatePage
import pages.external.OfficeOfDestinationPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DateTimeService
import utils.Format.RichLocalDate
import views.html.authorisationsAndLimit.limit.LimitDateView

import java.time.LocalDate
import scala.concurrent.Future

class LimitDateControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val dateTimeService = injector.instanceOf[DateTimeService]

  private val minDate    = dateTimeService.plusMinusDays(frontendAppConfig.limitDateDaysBefore)
  private val maxDate    = dateTimeService.plusMinusDays(frontendAppConfig.limitDateDaysAfter)
  private val maxDateArg = maxDate.plusDays(1).formatForText

  private val formProvider        = new DateFormProvider()
  private val form                = formProvider("authorisationsAndLimit.limit.limitDate", minDate, maxDate)
  private val mode                = NormalMode
  private lazy val limitDateRoute = routes.LimitDateController.onPageLoad(lrn, mode).url
  private val date                = LocalDate.now
  private val officeOfDestination = CustomsOffice("1D3", "OfficeOne", None)

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportNavigatorProvider]).toInstance(fakeTransportNavigatorProvider))

  "LimitDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.setValue(OfficeOfDestinationPage, officeOfDestination)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, limitDateRoute)

      val result = route(app, request).value

      val view = injector.instanceOf[LimitDateView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, lrn, mode, maxDateArg, officeOfDestination.toString)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(LimitDatePage, date).setValue(OfficeOfDestinationPage, officeOfDestination)

      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, limitDateRoute)

      val result = route(app, request).value

      val filledForm = form.bind(
        Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )
      )

      val view = injector.instanceOf[LimitDateView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, maxDateArg, officeOfDestination.toString)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers.setValue(OfficeOfDestinationPage, officeOfDestination))

      when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))

      val request = FakeRequest(POST, limitDateRoute)
        .withFormUrlEncodedBody(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers.setValue(OfficeOfDestinationPage, officeOfDestination))

      val invalidAnswer = ""

      val request    = FakeRequest(POST, limitDateRoute).withFormUrlEncodedBody(("value", ""))
      val filledForm = form.bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[LimitDateView]

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, maxDateArg, officeOfDestination.toString)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, limitDateRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, limitDateRoute)
        .withFormUrlEncodedBody(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
