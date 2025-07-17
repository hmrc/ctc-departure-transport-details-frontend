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
import models.reference.InlandMode
import models.reference.transportMeans.departure.Identification
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MeansOfTransportIdentificationTypesServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new MeansOfTransportIdentificationTypesService(mockRefDataConnector)

  private val identification1  = Identification("81", "Name of an inland waterways vehicle")
  private val identification2  = Identification("80", "European vessel identification number (ENI code)")
  private val identification3  = Identification("41", "Registration number of an aircraft")
  private val identification4  = Identification("40", "IATA flight number")
  private val identification5  = Identification("31", "Registration number of a road trailer")
  private val identification6  = Identification("30", "Registration number of a road vehicle")
  private val identification7  = Identification("21", "Train number")
  private val identification8  = Identification("20", "Wagon number")
  private val identification9  = Identification("11", "Name of a sea-going vessel")
  private val identification10 = Identification("10", "IMO ship identification number")

  private val identifications =
    NonEmptySet.of(
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

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "MeansOfTransportIdentificationTypesService" - {

    "getMeansOfTransportIdentificationTypes" - {
      "must return a full list of sorted identification types when InlandMode is Fixed" in {
        val inlandMode = Some(InlandMode("7", "Fixed transport installations - pipelines or electric power lines used for the continuous transport of goods"))

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(Right(identifications)))

        service.getMeansOfTransportIdentificationTypes(inlandMode).futureValue mustEqual
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
        val inlandMode = Some(InlandMode("1", "Maritime"))

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(Right(identifications)))

        service.getMeansOfTransportIdentificationTypes(inlandMode).futureValue mustEqual
          Seq(identification10, identification9)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }

      "must return a list of sorted identification types beginning with number 2 when InlandMode is Rail" in {
        val inlandMode = Some(InlandMode("2", "Rail"))

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(Right(identifications)))

        service.getMeansOfTransportIdentificationTypes(inlandMode).futureValue mustEqual
          Seq(identification8, identification7)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }

      "must return a list of sorted identification types beginning with number 3 when InlandMode is Road" in {
        val inlandMode = Some(InlandMode("3", "Road"))

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(Right(identifications)))

        service.getMeansOfTransportIdentificationTypes(inlandMode).futureValue mustEqual
          Seq(identification6, identification5)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }

      "must return a list of sorted identification types beginning with number 4 when InlandMode is Air" in {
        val inlandMode = Some(InlandMode("4", "Air"))

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(Right(identifications)))

        service.getMeansOfTransportIdentificationTypes(inlandMode).futureValue mustEqual
          Seq(identification4, identification3)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }

      "must return a list of sorted identification types beginning with number 8 when InlandMode is Inland waterway" in {
        val inlandMode = Some(InlandMode("8", "Inland waterway"))

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(Right(identifications)))

        service.getMeansOfTransportIdentificationTypes(inlandMode).futureValue mustEqual
          Seq(identification2, identification1)

        verify(mockRefDataConnector).getMeansOfTransportIdentificationTypes()(any(), any())
      }

      "must return a list of sorted identification types excluding Unknown identification when InlandMode is Fixed, Unknown or None" in {
        val inlandModeFixed =
          Some(InlandMode("7", "Fixed transport installations - pipelines or electric power lines used for the continuous transport of goods"))
        val inlandModeUnknown = Some(InlandMode("9", "Mode unknown (Own propulsion)"))
        val inlandModeNone    = None
        val inlandMode        = Gen.oneOf(inlandModeFixed, inlandModeUnknown, inlandModeNone).sample.value

        when(mockRefDataConnector.getMeansOfTransportIdentificationTypes()(any(), any()))
          .thenReturn(Future.successful(Right(identifications)))

        service.getMeansOfTransportIdentificationTypes(inlandMode).futureValue mustEqual
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
    }
  }
}
