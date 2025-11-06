/*
 * Copyright 2024 HM Revenue & Customs
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
import generators.Generators
import models.reference.additionalInformation.AdditionalInformationCode
import models.{Index, SelectableList}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.external.ItemCountryOfDestinationInCL009Page
import pages.preRequisites.ItemsDestinationCountryInCL009Page
import pages.sections.external.ConsigneeSection
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AdditionalInformationServiceSpec extends SpecBase with BeforeAndAfterEach with Generators {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new AdditionalInformationService(mockRefDataConnector)

  private val additionalInformationCode1: AdditionalInformationCode =
    AdditionalInformationCode("20100", "EFTA")

  private val additionalInformationCode2: AdditionalInformationCode =
    AdditionalInformationCode("20300", "Export")

  private val additionalInformationCode3: AdditionalInformationCode =
    AdditionalInformationCode("30600", "EXS")

  private val additionalInformationCodes: NonEmptySet[AdditionalInformationCode] =
    NonEmptySet.of(additionalInformationCode1, additionalInformationCode2, additionalInformationCode3)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "AdditionalInformationService" - {

    "getAdditionalInformationCodes" - {
      "must return a list of additional information codes" - {
        val expectedResult = SelectableList(Seq(additionalInformationCode1, additionalInformationCode2, additionalInformationCode3))

        "when user answers contains no information about consignment or item countries of destination" in {
          when(mockRefDataConnector.getAdditionalInformationCodes()(any(), any()))
            .thenReturn(Future.successful(Right(additionalInformationCodes)))

          service.getAdditionalInformationCodes(emptyUserAnswers).futureValue mustEqual expectedResult

          verify(mockRefDataConnector).getAdditionalInformationCodes()(any(), any())
        }

        "when consignment country of destination is not in CL009" in {
          when(mockRefDataConnector.getAdditionalInformationCodes()(any(), any()))
            .thenReturn(Future.successful(Right(additionalInformationCodes)))

          val userAnswers = emptyUserAnswers.setValue(ItemsDestinationCountryInCL009Page, false)

          service.getAdditionalInformationCodes(userAnswers).futureValue mustEqual expectedResult

          verify(mockRefDataConnector).getAdditionalInformationCodes()(any(), any())
        }

        "when no item countries of destination are in CL009" in {
          when(mockRefDataConnector.getAdditionalInformationCodes()(any(), any()))
            .thenReturn(Future.successful(Right(additionalInformationCodes)))

          val userAnswers = emptyUserAnswers
            .setValue(ItemCountryOfDestinationInCL009Page(Index(0)), false)
            .setValue(ItemCountryOfDestinationInCL009Page(Index(1)), false)
            .setValue(ItemCountryOfDestinationInCL009Page(Index(2)), false)

          service.getAdditionalInformationCodes(userAnswers).futureValue mustEqual expectedResult

          verify(mockRefDataConnector).getAdditionalInformationCodes()(any(), any())
        }

        "when consignee details are not present" in {
          when(mockRefDataConnector.getAdditionalInformationCodes()(any(), any()))
            .thenReturn(Future.successful(Right(additionalInformationCodes)))

          val userAnswers = emptyUserAnswers.setValue(ConsigneeSection, Json.obj())

          service.getAdditionalInformationCodes(userAnswers).futureValue mustEqual expectedResult

          verify(mockRefDataConnector).getAdditionalInformationCodes()(any(), any())
        }
      }

      "must return a list of additional information codes with 30600 filtered out" - {
        val expectedResult = SelectableList(Seq(additionalInformationCode1, additionalInformationCode2))

        "when consignment country of destination is in CL009" in {
          when(mockRefDataConnector.getAdditionalInformationCodes()(any(), any()))
            .thenReturn(Future.successful(Right(additionalInformationCodes)))

          val userAnswers = emptyUserAnswers.setValue(ItemsDestinationCountryInCL009Page, true)

          service.getAdditionalInformationCodes(userAnswers).futureValue mustEqual expectedResult

          verify(mockRefDataConnector).getAdditionalInformationCodes()(any(), any())
        }

        "when one item country of destination is in CL009" in {
          when(mockRefDataConnector.getAdditionalInformationCodes()(any(), any()))
            .thenReturn(Future.successful(Right(additionalInformationCodes)))

          val userAnswers = emptyUserAnswers
            .setValue(ItemCountryOfDestinationInCL009Page(Index(0)), true)
            .setValue(ItemCountryOfDestinationInCL009Page(Index(1)), false)
            .setValue(ItemCountryOfDestinationInCL009Page(Index(2)), false)

          service.getAdditionalInformationCodes(userAnswers).futureValue mustEqual expectedResult

          verify(mockRefDataConnector).getAdditionalInformationCodes()(any(), any())
        }

        "when all item countries of destination are in CL009" in {
          when(mockRefDataConnector.getAdditionalInformationCodes()(any(), any()))
            .thenReturn(Future.successful(Right(additionalInformationCodes)))

          val userAnswers = emptyUserAnswers
            .setValue(ItemCountryOfDestinationInCL009Page(Index(0)), true)
            .setValue(ItemCountryOfDestinationInCL009Page(Index(1)), true)
            .setValue(ItemCountryOfDestinationInCL009Page(Index(2)), true)

          service.getAdditionalInformationCodes(userAnswers).futureValue mustEqual expectedResult

          verify(mockRefDataConnector).getAdditionalInformationCodes()(any(), any())
        }

        "when consignee details are present" in {
          when(mockRefDataConnector.getAdditionalInformationCodes()(any(), any()))
            .thenReturn(Future.successful(Right(additionalInformationCodes)))

          val userAnswers = emptyUserAnswers.setValue(ConsigneeSection, Json.obj("foo" -> "bar"))

          service.getAdditionalInformationCodes(userAnswers).futureValue mustEqual expectedResult

          verify(mockRefDataConnector).getAdditionalInformationCodes()(any(), any())
        }
      }
    }
  }
}
