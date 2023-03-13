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

package models.transportMeans.active

import base.SpecBase
import generators.Generators
import models.Index
import models.transportMeans.BorderModeOfTransport
import models.transportMeans.BorderModeOfTransport._
import models.transportMeans.active.Identification._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.BorderModeOfTransportPage
import play.api.libs.json.{JsError, JsString, Json}

class IdentificationSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Identification" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(Identification.values)

      forAll(gen) {
        identification =>
          JsString(identification.toString).validate[Identification].asOpt.value mustEqual identification
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!Identification.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[Identification] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(Identification.values)

      forAll(gen) {
        identification =>
          Json.toJson(identification) mustEqual JsString(identification.toString)
      }
    }

    "values" - {
      "when index 0" - {
        val index = Index(0)

        "and border mode of transport undefined" - {
          "must return all values" in {
            Identification.values(emptyUserAnswers, index) mustBe Seq(
              ImoShipIdNumber,
              SeaGoingVessel,
              TrainNumber,
              RegNumberRoadVehicle,
              IataFlightNumber,
              RegNumberAircraft,
              EuropeanVesselIdNumber,
              InlandWaterwaysVehicle
            )
          }
        }

        "and border mode of transport is Sea" - {
          "must return ImoShipIdNumber and SeaGoingVessel" in {
            val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, Sea)
            Identification.values(userAnswers, index) mustBe Seq(
              ImoShipIdNumber,
              SeaGoingVessel
            )
          }
        }

        "and border mode of transport is Air" - {
          "must return IataFlightNumber and RegNumberAircraft" in {
            val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, Air)
            Identification.values(userAnswers, index) mustBe Seq(
              IataFlightNumber,
              RegNumberAircraft
            )
          }
        }

        "and border mode of transport is ChannelTunnel" - {
          "must return TrainNumber" in {
            val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, ChannelTunnel)
            Identification.values(userAnswers, index) mustBe Seq(
              TrainNumber
            )
          }
        }

        "and border mode of transport is IrishLandBoundary" - {
          "wmust return RegNumberRoadVehicle" in {
            val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, IrishLandBoundary)
            Identification.values(userAnswers, index) mustBe Seq(
              RegNumberRoadVehicle
            )
          }
        }
      }

      "when index is not 0" - {
        val index = Gen.choose(1, frontendAppConfig.maxActiveBorderTransports).sample.value

        "must return all values" in {
          forAll(arbitrary[BorderModeOfTransport]) {
            borderModeOfTransport =>
              val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, borderModeOfTransport)
              Identification.values(userAnswers, Index(index)) mustBe Seq(
                ImoShipIdNumber,
                SeaGoingVessel,
                TrainNumber,
                RegNumberRoadVehicle,
                IataFlightNumber,
                RegNumberAircraft,
                EuropeanVesselIdNumber,
                InlandWaterwaysVehicle
              )
          }
        }
      }
    }
  }
}
