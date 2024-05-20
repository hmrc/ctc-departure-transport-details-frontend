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
import models.SelectableList
import models.reference.additionalInformation.AdditionalInformationCode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.preRequisites.ItemsDestinationCountryInCL009Page

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AdditionalInformationServiceSpec extends SpecBase with BeforeAndAfterEach with Generators {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new AdditionalInformationService(mockRefDataConnector)

  private val additionalInformationCode1: AdditionalInformationCode =
    AdditionalInformationCode("20100", "Export from one EFTA country subject to restriction or export from the Union subject to restriction")
  private val additionalInformationCode2: AdditionalInformationCode = AdditionalInformationCode("20300", "Export")

  val additionalInformationCode3 = AdditionalInformationCode(
    "30600",
    "In EXS, where negotiable bills of lading 'to order blank endorsed' are concerned and the consignee particulars are unknown."
  )

  private val additionalInformationCodes: NonEmptySet[AdditionalInformationCode] =
    NonEmptySet.of(additionalInformationCode1, additionalInformationCode2, additionalInformationCode3)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "AdditionalInformationService" - {

    "getAdditionalInformationCodes" - {
      "must return a list of additional information codes" in {

        when(mockRefDataConnector.getAdditionalInformationCodes()(any(), any()))
          .thenReturn(Future.successful(additionalInformationCodes))

        service.getAdditionalInformationCodes(emptyUserAnswers).futureValue mustBe
          SelectableList(Seq(additionalInformationCode1, additionalInformationCode2, additionalInformationCode3))

        verify(mockRefDataConnector).getAdditionalInformationCodes()(any(), any())
      }

      "must return a list of additional information codes with filtered additionalInformationCode 30600" in {

        when(mockRefDataConnector.getAdditionalInformationCodes()(any(), any()))
          .thenReturn(Future.successful(additionalInformationCodes))

        val userAnswers = emptyUserAnswers.setValue(ItemsDestinationCountryInCL009Page, true)

        service.getAdditionalInformationCodes(userAnswers).futureValue mustBe
          SelectableList(Seq(additionalInformationCode1, additionalInformationCode2))

        verify(mockRefDataConnector).getAdditionalInformationCodes()(any(), any())

      }
    }
  }
}
