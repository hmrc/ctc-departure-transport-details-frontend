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

package controllers.equipment.index

import base.{AppWithDefaultMockFixtures, SpecBase}
import forms.ContainerIdentificationNumberFormProvider
import models.{Index, NormalMode, UserAnswers}
import navigation.EquipmentNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.equipment.index.{ContainerIdentificationNumberPage, UuidPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.equipment.index.ContainerIdentificationNumberView

import java.util.UUID
import scala.concurrent.Future

class ContainerIdentificationNumberControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider                            = new ContainerIdentificationNumberFormProvider()
  private def form(otherIds: Seq[String] = Nil)       = formProvider("equipment.index.containerIdentificationNumber", otherIds)
  private val validAnswer                             = "testString"
  private val mode                                    = NormalMode
  private def identificationNumberRoute(index: Index) = routes.ContainerIdentificationNumberController.onPageLoad(lrn, mode, index).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[EquipmentNavigatorProvider]).toInstance(fakeEquipmentNavigatorProvider))

  "IdentificationNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(GET, identificationNumberRoute(equipmentIndex))

      val result = route(app, request).value

      val view = injector.instanceOf[ContainerIdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form(), lrn, mode, equipmentIndex)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(ContainerIdentificationNumberPage(equipmentIndex), validAnswer)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, identificationNumberRoute(equipmentIndex))

      val result = route(app, request).value

      val filledForm = form().bind(Map("value" -> validAnswer))

      val view = injector.instanceOf[ContainerIdentificationNumberView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, equipmentIndex)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" - {

      "and UUID not populated" in {
        when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))

        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, identificationNumberRoute(equipmentIndex))
          .withFormUrlEncodedBody(("value", validAnswer))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          s"http://localhost:10127/manage-transit-movements/departures/items/$lrn/update-task?" +
          s"continue=http://localhost:10131${onwardRoute.url}"

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())

        userAnswersCaptor.getValue.get(UuidPage(equipmentIndex)) must be(defined)
      }

      "and UUID populated" in {
        when(mockSessionRepository.set(any())(any())).thenReturn(Future.successful(true))

        val uuid        = arbitrary[UUID].sample.value
        val userAnswers = emptyUserAnswers.setValue(UuidPage(equipmentIndex), uuid)
        setExistingUserAnswers(userAnswers)

        val request = FakeRequest(POST, identificationNumberRoute(equipmentIndex))
          .withFormUrlEncodedBody(("value", validAnswer))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          s"http://localhost:10127/manage-transit-movements/departures/items/$lrn/update-task?" +
          s"continue=http://localhost:10131${onwardRoute.url}"

        val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())

        userAnswersCaptor.getValue.get(UuidPage(equipmentIndex)).value mustEqual uuid
      }
    }

    "when duplicate value is submitted" in {
      val userAnswers = emptyUserAnswers.setValue(ContainerIdentificationNumberPage(equipmentIndex), validAnswer)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(POST, identificationNumberRoute(Index(1)))
        .withFormUrlEncodedBody(("value", validAnswer))

      val filledForm = form(Seq(validAnswer)).bind(Map("value" -> validAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[ContainerIdentificationNumberView]

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, Index(1))(request, messages).toString
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val invalidAnswer = ""

      val request    = FakeRequest(POST, identificationNumberRoute(equipmentIndex)).withFormUrlEncodedBody(("value", ""))
      val filledForm = form().bind(Map("value" -> invalidAnswer))

      val result = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val view = injector.instanceOf[ContainerIdentificationNumberView]

      contentAsString(result) mustEqual
        view(filledForm, lrn, mode, equipmentIndex)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, identificationNumberRoute(equipmentIndex))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, identificationNumberRoute(equipmentIndex))
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
    }
  }
}
