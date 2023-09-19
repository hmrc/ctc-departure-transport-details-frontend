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
import models.reference.transportMeans.active.Identification
import models.transportMeans.BorderModeOfTransport.{Air, ChannelTunnel, IrishLandBoundary, Sea}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MeansOfTransportIdentificationTypesActiveServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new MeansOfTransportIdentificationTypesActiveService(mockRefDataConnector)

  private val identification1 = Identification("41", "Registration number of an aircraft")
  private val identification2 = Identification("40", "IATA flight number")
  private val identification3 = Identification("30", "Registration number of a road vehicle")
  private val identification4 = Identification("21", "Train number")
  private val identification5 = Identification("11", "Name of a sea-going vessel")
  private val identification6 = Identification("10", "IMO ship identification number")

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "MeansOfTransportIdentificationTypesService" - {

    "getMeansOfTransportIdentificationTypes" - {
      "must return a list of sorted identification types beginning with number 1 when BorderModeOfTransport is Sea" in {
        val borderMode = Sea

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypesActive()(any(), any()))
          .thenReturn(Future.successful(Seq(identification5, identification6)))

        service.getMeansOfTransportIdentificationTypesActive(activeIndex, Some(borderMode)).futureValue mustBe
          Seq(identification6, identification5)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypesActive()(any(), any())
      }

      "must return a list of sorted identification types beginning with number 2 when BorderModeOfTransport is ChannelTunnel" in {
        val borderMode = ChannelTunnel

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypesActive()(any(), any()))
          .thenReturn(Future.successful(Seq(identification4)))

        service.getMeansOfTransportIdentificationTypesActive(activeIndex, Some(borderMode)).futureValue mustBe
          Seq(identification4)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypesActive()(any(), any())
      }

      "must return a list of sorted identification types beginning with number 3 when BorderModeOfTransport is IrishLandBoundary" in {
        val borderMode = IrishLandBoundary

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypesActive()(any(), any()))
          .thenReturn(Future.successful(Seq(identification3)))

        service.getMeansOfTransportIdentificationTypesActive(activeIndex, Some(borderMode)).futureValue mustBe
          Seq(identification3)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypesActive()(any(), any())
      }

      "must return a list of sorted identification types beginning with number 4 when BorderModeOfTransport is Air" in {
        val borderMode = Air

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypesActive()(any(), any()))
          .thenReturn(Future.successful(Seq(identification1, identification2)))

        service.getMeansOfTransportIdentificationTypesActive(activeIndex, Some(borderMode)).futureValue mustBe
          Seq(identification2, identification1)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypesActive()(any(), any())
      }

      "must return a full list of sorted identification types when BorderModeOfTransport is None" in {

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypesActive()(any(), any()))
          .thenReturn(Future.successful(Seq(identification1, identification2, identification3, identification4)))

        service.getMeansOfTransportIdentificationTypesActive(activeIndex, None).futureValue mustBe
          Seq(identification4, identification3, identification2, identification1)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypesActive()(any(), any())
      }
    }
  }
}
