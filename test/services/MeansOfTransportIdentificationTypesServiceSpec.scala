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
import config.Constants._
import connectors.ReferenceDataConnector
import models.reference.InlandMode
import models.reference.transportMeans.departure.Identification
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MeansOfTransportIdentificationTypesServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new MeansOfTransportIdentificationTypesService(mockRefDataConnector)

  private val identification1  = Identification("81", "Name of the inland waterways vessel")
  private val identification2  = Identification("80", "European Vessel Identification Number (ENI Code)")
  private val identification3  = Identification("41", "Registration Number of the Aircraft")
  private val identification4  = Identification("40", "IATA flight number")
  private val identification5  = Identification("31", "Registration Number of the Road Trailer")
  private val identification6  = Identification("30", "Registration Number of the Road Vehicle")
  private val identification7  = Identification("21", "Train Number")
  private val identification8  = Identification("20", "Wagon Number")
  private val identification9  = Identification("11", "Name of the sea-going vessel")
  private val identification10 = Identification("10", "IMO Ship Identification Number")

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "MeansOfTransportIdentificationTypesService" - {

    "getMeansOfTransportIdentificationTypes" - {
      "must return a full list of sorted identification types when InlandMode is Fixed" in {
        val inlandMode = InlandMode(Fixed, "Fixed transport installations - pipelines or electric power lines used for the continuous transport of goods")

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(
            Future.successful(
              Seq(
                identification1,
                identification2,
                identification3,
                identification4,
                identification5,
                identification6,
                identification7,
                identification8,
                identification9,
                identification10
              )
            )
          )

        service.getMeansOfTransportIdentificationTypesService(inlandMode).futureValue mustBe
          Seq(
            identification10,
            identification9,
            identification8,
            identification7,
            identification6,
            identification5,
            identification4,
            identification3,
            identification2,
            identification1
          )

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }

      "must return a list of sorted identification types beginning with number 1 when InlandMode is Maritime" in {
        val inlandMode = InlandMode(Maritime, "Maritime")

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(Seq(identification9, identification10)))

        service.getMeansOfTransportIdentificationTypesService(inlandMode).futureValue mustBe
          Seq(identification10, identification9)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }

      "must return a list of sorted identification types beginning with number 2 when InlandMode is Rail" in {
        val inlandMode = InlandMode(Rail, "Rail")

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(Seq(identification7, identification8)))

        service.getMeansOfTransportIdentificationTypesService(inlandMode).futureValue mustBe
          Seq(identification8, identification7)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }

      "must return a list of sorted identification types beginning with number 3 when InlandMode is Road" in {
        val inlandMode = InlandMode(Road, "Road")

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(Seq(identification5, identification6)))

        service.getMeansOfTransportIdentificationTypesService(inlandMode).futureValue mustBe
          Seq(identification6, identification5)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }

      "must return a list of sorted identification types beginning with number 4 when InlandMode is Air" in {
        val inlandMode = InlandMode(Air, "Air")

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(Seq(identification3, identification4)))

        service.getMeansOfTransportIdentificationTypesService(inlandMode).futureValue mustBe
          Seq(identification4, identification3)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }

      "must return a list of sorted identification types beginning with number 8 when InlandMode is Inland waterway" in {
        val inlandMode = InlandMode(Waterway, "Inland waterway")

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(Seq(identification1, identification2)))

        service.getMeansOfTransportIdentificationTypesService(inlandMode).futureValue mustBe
          Seq(identification2, identification1)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }
    }
  }
}
