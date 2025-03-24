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
import cats.data.NonEmptySet
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

  private val c524 = AuthorisationType(
    "C524",
    "TRD - authorisation to use transit declaration with a reduced dataset"
  )

  private val c523 = AuthorisationType(
    "C523",
    "SSE - authorisation for the use of seals of a special type"
  )

  private val c521 = AuthorisationType(
    "C521",
    "ACR - authorisation for the status of authorised consignor for Union transit"
  )

  private val authorisationTypes = NonEmptySet.of(c521, c523, c524)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "AuthorisationTypesService" - {

    "getAuthorisationTypes" - {

      "when no authorisation types in user answers" - {

        "must return the full sorted list of authorisation types" in {
          when(mockRefDataConnector.getAuthorisationTypes()(any(), any()))
            .thenReturn(Future.successful(Right(authorisationTypes)))

          val userAnswers = emptyUserAnswers

          service.getAuthorisationTypes(userAnswers, Some(Index(0))).futureValue mustBe Seq(c523, c524)

          verify(mockRefDataConnector).getAuthorisationTypes()(any(), any())
        }
      }

      "when there is an authorisation type in user answers" - {
        "must filter out that type" - {
          "when it has been inferred" in {
            when(mockRefDataConnector.getAuthorisationTypes()(any(), any()))
              .thenReturn(Future.successful(Right(authorisationTypes)))

            val userAnswers = emptyUserAnswers
              .setValue(InferredAuthorisationTypePage(Index(0)), c521)

            service.getAuthorisationTypes(userAnswers, Some(Index(1))).futureValue mustBe Seq(c523, c524)

            verify(mockRefDataConnector).getAuthorisationTypes()(any(), any())
          }

          "when it has not been inferred" in {
            when(mockRefDataConnector.getAuthorisationTypes()(any(), any()))
              .thenReturn(Future.successful(Right(authorisationTypes)))

            val userAnswers = emptyUserAnswers
              .setValue(AuthorisationTypePage(Index(0)), c521)

            service.getAuthorisationTypes(userAnswers, Some(Index(1))).futureValue mustBe Seq(c523, c524)

            verify(mockRefDataConnector).getAuthorisationTypes()(any(), any())
          }
        }
      }

      "when all 3 auth types are in user answers" - {
        "must return empty list" in {
          when(mockRefDataConnector.getAuthorisationTypes()(any(), any()))
            .thenReturn(Future.successful(Right(authorisationTypes)))

          val userAnswers = emptyUserAnswers
            .setValue(InferredAuthorisationTypePage(Index(0)), c521)
            .setValue(AuthorisationTypePage(Index(1)), c523)
            .setValue(InferredAuthorisationTypePage(Index(2)), c524)

          service.getAuthorisationTypes(userAnswers, Some(Index(3))).futureValue mustBe Seq.empty

          verify(mockRefDataConnector).getAuthorisationTypes()(any(), any())
        }
      }

      "when one auth type has been added and we are that index" - {
        "must return all 3 auth types" in {
          when(mockRefDataConnector.getAuthorisationTypes()(any(), any()))
            .thenReturn(Future.successful(Right(authorisationTypes)))

          val userAnswers = emptyUserAnswers
            .setValue(InferredAuthorisationTypePage(Index(0)), c524)

          service.getAuthorisationTypes(userAnswers, Some(Index(0))).futureValue mustBe Seq(c523, c524)

          verify(mockRefDataConnector).getAuthorisationTypes()(any(), any())
        }
      }
    }

    "getAll" - {
      "must return all authorisation types" in {
        when(mockRefDataConnector.getAuthorisationTypes()(any(), any()))
          .thenReturn(Future.successful(Right(authorisationTypes)))

        service.getAuthorisationTypes().futureValue mustBe NonEmptySet.of(c521, c523, c524)

        verify(mockRefDataConnector).getAuthorisationTypes()(any(), any())
      }
    }
  }
}
