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
import forms.YesNoFormProvider
import generators.Generators
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.equipment.index.{AddContainerIdentificationNumberYesNoPage, ContainerIdentificationNumberPage}
import pages.equipment.index.seals.IdentificationNumberPage
import pages.sections.equipment.EquipmentSection
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.equipment.index.RemoveTransportEquipmentView

import scala.concurrent.Future

class RemoveTransportEquipmentControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val formProvider                       = new YesNoFormProvider()
  private val form                               = formProvider("equipment.index.removeTransportEquipment", equipmentIndex.display)
  private val mode                               = NormalMode
  private lazy val removeTransportEquipmentRoute = routes.RemoveTransportEquipmentController.onPageLoad(lrn, mode, equipmentIndex).url

  "RemoveTransportEquipment Controller" - {

    "must return OK and the correct view for a GET" in {

      forAll(Gen.alphaNumStr) {
        containerId =>
          val userAnswers = emptyUserAnswers
            .setValue(IdentificationNumberPage(equipmentIndex, sealIndex), "Seal-1")
            .setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), true)
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), containerId)

          setExistingUserAnswers(userAnswers)

          val request = FakeRequest(GET, removeTransportEquipmentRoute)
          val result  = route(app, request).value

          val view      = injector.instanceOf[RemoveTransportEquipmentView]
          val insetText = Some(s"Container $containerId")

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, lrn, mode, equipmentIndex, insetText)(request, messages).toString
      }
    }

    "must redirect to the next page" - {
      "when yes is submitted" in {
        forAll(arbitraryEquipmentAnswers(emptyUserAnswers, equipmentIndex)) {
          userAnswers =>
            reset(mockSessionRepository)
            when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

            setExistingUserAnswers(userAnswers)

            val request = FakeRequest(POST, removeTransportEquipmentRoute)
              .withFormUrlEncodedBody(("value", "true"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.equipment.routes.AddAnotherEquipmentController.onPageLoad(lrn, mode).url

            val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
            userAnswersCaptor.getValue.get(EquipmentSection(equipmentIndex)) mustNot be(defined)
        }
      }

      "when no is submitted" in {
        forAll(arbitraryEquipmentAnswers(emptyUserAnswers, equipmentIndex)) {
          userAnswers =>
            reset(mockSessionRepository)
            when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)

            setExistingUserAnswers(userAnswers)

            val request = FakeRequest(POST, removeTransportEquipmentRoute)
              .withFormUrlEncodedBody(("value", "false"))

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.equipment.routes.AddAnotherEquipmentController.onPageLoad(lrn, mode).url

            verify(mockSessionRepository, never()).set(any())(any())
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      forAll(Gen.alphaNumStr) {
        containerId =>
          val userAnswers = emptyUserAnswers
            .setValue(IdentificationNumberPage(equipmentIndex, sealIndex), "Seal-1")
            .setValue(AddContainerIdentificationNumberYesNoPage(equipmentIndex), true)
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), containerId)

          setExistingUserAnswers(userAnswers)

          val request   = FakeRequest(POST, removeTransportEquipmentRoute).withFormUrlEncodedBody(("value", ""))
          val boundForm = form.bind(Map("value" -> ""))

          val result    = route(app, request).value
          val insetText = Some(s"Container $containerId")

          status(result) mustEqual BAD_REQUEST

          val view = injector.instanceOf[RemoveTransportEquipmentView]

          contentAsString(result) mustEqual
            view(boundForm, lrn, mode, equipmentIndex, insetText)(request, messages).toString
      }
    }

    "must redirect to Session Expired for a GET" - {
      "when no existing data is found" in {
        setNoExistingUserAnswers()

        val request = FakeRequest(GET, removeTransportEquipmentRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }

      "when no transport equipment is found" in {
        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, removeTransportEquipmentRoute)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.equipment.routes.AddAnotherEquipmentController.onPageLoad(lrn, mode).url
      }
    }

    "must redirect to Session Expired for a POST" - {
      "when no existing data is found" in {
        setNoExistingUserAnswers()

        val request = FakeRequest(POST, removeTransportEquipmentRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl(lrn)
      }

      "when no transport equipment is found" in {
        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, removeTransportEquipmentRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.equipment.routes.AddAnotherEquipmentController.onPageLoad(lrn, mode).url
      }
    }
  }
}
