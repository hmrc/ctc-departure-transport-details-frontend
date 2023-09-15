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

package services

import base.SpecBase
import connectors.ReferenceDataConnector
import models.Index
import models.reference.authorisations.AuthorisationType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.authorisationsAndLimit.authorisations.index.{AuthorisationTypePage, InferredAuthorisationTypePage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthorisationTypesServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new AuthorisationTypesService(mockRefDataConnector)

  private val authorisationType1 = AuthorisationType(
    "C524",
    "TRD - authorisation to use transit declaration with a reduced dataset"
  )

  private val authorisationType2 =
    AuthorisationType("C523", "SSE - authorisation for the use of seals of a special type")

  private val authorisationType3 = AuthorisationType(
    "C521",
    "ACR - authorisation for the status of authorised consignor for Union transit"
  )

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "AuthorisationTypesService" - {

    "getAuthorisationTypes" - {

      "when no authorisation types in user answers" - {

        "must return the full sorted list of authorisation types" in {
          when(mockRefDataConnector.getAuthorisationTypes()(any(), any()))
            .thenReturn(Future.successful(Seq(authorisationType1, authorisationType2, authorisationType3)))

          val userAnswers = emptyUserAnswers

          service.getAuthorisationTypes(userAnswers, Index(0)).futureValue mustBe Seq(authorisationType3, authorisationType2, authorisationType1)

          verify(mockRefDataConnector).getAuthorisationTypes()(any(), any())
        }
      }

      "when there is an authorisation type in user answers" - {
        "must filter out that type" - {
          "when it has been inferred" in {
            when(mockRefDataConnector.getAuthorisationTypes()(any(), any()))
              .thenReturn(Future.successful(Seq(authorisationType1, authorisationType2)))

            val userAnswers = emptyUserAnswers
              .setValue(InferredAuthorisationTypePage(Index(0)), authorisationType3)

            service.getAuthorisationTypes(userAnswers, Index(1)).futureValue mustBe Seq(authorisationType2, authorisationType1)

            verify(mockRefDataConnector).getAuthorisationTypes()(any(), any())
          }

          "when it has not been inferred" in {
            when(mockRefDataConnector.getAuthorisationTypes()(any(), any()))
              .thenReturn(Future.successful(Seq(authorisationType1, authorisationType2)))

            val userAnswers = emptyUserAnswers
              .setValue(AuthorisationTypePage(Index(0)), authorisationType3)

            service.getAuthorisationTypes(userAnswers, Index(1)).futureValue mustBe Seq(authorisationType2, authorisationType1)

            verify(mockRefDataConnector).getAuthorisationTypes()(any(), any())
          }
        }
      }

      "when all 3 auth types are in user answers" - {
        "must return empty list" in {
          when(mockRefDataConnector.getAuthorisationTypes()(any(), any()))
            .thenReturn(Future.successful(Seq.empty))

          val userAnswers = emptyUserAnswers
            .setValue(InferredAuthorisationTypePage(Index(0)), authorisationType3)
            .setValue(AuthorisationTypePage(Index(1)), authorisationType2)
            .setValue(InferredAuthorisationTypePage(Index(2)), authorisationType1)

          service.getAuthorisationTypes(userAnswers, Index(1)).futureValue mustBe Seq.empty

          verify(mockRefDataConnector).getAuthorisationTypes()(any(), any())
        }
      }

      "when one auth type has been added and we are that index" - {
        "must return all 3 auth types" in {
          when(mockRefDataConnector.getAuthorisationTypes()(any(), any()))
            .thenReturn(Future.successful(Seq(authorisationType1, authorisationType2, authorisationType3)))

          val userAnswers = emptyUserAnswers
            .setValue(InferredAuthorisationTypePage(Index(0)), authorisationType3)

          service.getAuthorisationTypes(userAnswers, Index(0)).futureValue mustBe Seq(authorisationType3, authorisationType2, authorisationType1)

          verify(mockRefDataConnector).getAuthorisationTypes()(any(), any())
        }
      }
    }

    "getAll" - {
      "must return all authorisation types" in {
        when(mockRefDataConnector.getAuthorisationTypes()(any(), any()))
          .thenReturn(Future.successful(Seq(authorisationType1, authorisationType2, authorisationType3)))

        service.getAll().futureValue mustBe Seq(authorisationType3, authorisationType2, authorisationType1)

        verify(mockRefDataConnector).getAuthorisationTypes()(any(), any())
      }
    }
  }
}
