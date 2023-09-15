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

package controllers.authorisationsAndLimit

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.reference.authorisations.AuthorisationType
import models.{NormalMode, TaskStatus, UserAnswers}
import navigation.TransportNavigatorProvider
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.authorisationsAndLimit.AuthorisationsInferredPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuthorisationInferenceService

class AuthorisationInferenceControllerSpec extends SpecBase with AppWithDefaultMockFixtures with MockitoSugar with Generators {

  private val authorisationTypes = arbitrary[Seq[AuthorisationType]].sample.value

  private val mode                             = NormalMode
  private lazy val authorisationInferenceRoute = routes.AuthorisationInferenceController.infer(lrn, mode).url

  private lazy val mockAuthorisationInferenceService = mock[AuthorisationInferenceService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[TransportNavigatorProvider]).toInstance(fakeTransportNavigatorProvider))
      .overrides(bind(classOf[AuthorisationInferenceService]).toInstance(mockAuthorisationInferenceService))

  "AuthorisationInference Controller" - {

    "must update user answers and redirect" in {

      val userAnswersBeforeInference = emptyUserAnswers
      setExistingUserAnswers(userAnswersBeforeInference)

      val userAnswersAfterInference = userAnswersBeforeInference.copy(data = Json.obj("foo" -> "bar"))
      when(mockAuthorisationInferenceService.inferAuthorisations(any(), any())).thenReturn(userAnswersAfterInference)

      val request = FakeRequest(GET, authorisationInferenceRoute)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      verify(mockAuthorisationInferenceService).inferAuthorisations(eqTo(userAnswersBeforeInference), authorisationTypes)

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(userAnswersCaptor.capture())(any())
      userAnswersCaptor.getValue.data mustBe userAnswersAfterInference.setValue(AuthorisationsInferredPage, true).data
      userAnswersCaptor.getValue.tasks.get(".transportDetails").value mustBe TaskStatus.InProgress
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, authorisationInferenceRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual frontendAppConfig.sessionExpiredUrl
    }
  }
}
