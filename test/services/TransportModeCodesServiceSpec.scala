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
import models.reference.InlandMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TransportModeCodesServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new TransportModeCodesService(mockRefDataConnector)

  private val inlandMode1 = InlandMode("7", "Inland waterway")
  private val inlandMode2 = InlandMode("6", "Fixed transport installations - pipelines or electric power lines used for the continuous transport of goods")
  private val inlandMode3 = InlandMode("5", "Mail (active mode of transport unknown)")
  private val inlandMode4 = InlandMode("4", "Air")
  private val inlandMode5 = InlandMode("3", "Road")
  private val inlandMode6 = InlandMode("2", "Rail")
  private val inlandMode7 = InlandMode("1", "Maritime")

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "TransportModeCodesService" - {

    "getTransportModeCodes" - {
      "must return a list of sorted inland modes" in {
        when(mockRefDataConnector.getTransportModeCodes()(any(), any()))
          .thenReturn(Future.successful(Seq(inlandMode1, inlandMode2, inlandMode3, inlandMode4, inlandMode5, inlandMode6, inlandMode7)))

        service.getTransportModeCodes().futureValue mustBe
          Seq(inlandMode7, inlandMode6, inlandMode5, inlandMode4, inlandMode3, inlandMode2, inlandMode1)

        verify(mockRefDataConnector).getTransportModeCodes()(any(), any())
      }
    }
  }
}
