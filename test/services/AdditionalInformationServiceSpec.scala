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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AdditionalInformationServiceSpec extends SpecBase with BeforeAndAfterEach with Generators {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new AdditionalInformationService(mockRefDataConnector)

  private val informationCode1: AdditionalInformationCode =
    AdditionalInformationCode("20100", "Export from one EFTA country subject to restriction or export from the Union subject to restriction")
  private val informationCode2: AdditionalInformationCode                        = AdditionalInformationCode("20300", "Export")
  private val additionalInformationCodes: NonEmptySet[AdditionalInformationCode] = NonEmptySet.of(informationCode1, informationCode2)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "AdditionalInformationService" - {

    "getAdditionalInformationCodes" - {
      "must return a list of additional information codes" in {

        when(mockRefDataConnector.getAdditionalInformationCodes()(any(), any()))
          .thenReturn(Future.successful(additionalInformationCodes))

        service.getAdditionalInformationCodes().futureValue mustBe
          SelectableList(Seq(informationCode1, informationCode2))

        verify(mockRefDataConnector).getAdditionalInformationCodes()(any(), any())
      }
    }
  }
}
