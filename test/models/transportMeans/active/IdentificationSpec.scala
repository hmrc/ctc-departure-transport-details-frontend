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
import models.transportMeans.BorderModeOfTransport._
import models.transportMeans.active.Identification._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.BorderModeOfTransportPage
import play.api.libs.json.{JsError, JsString, Json}

class IdentificationSpec extends SpecBase with ScalaCheckPropertyChecks {

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

    "valuesU" - {
      "must return all values" - {
        "when border mode of transport undefined" in {
          Identification.valuesU(emptyUserAnswers) mustBe Seq(
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

      "must return ImoShipIdNumber and SeaGoingVessel" - {
        "when border mode of transport is Sea" in {
          val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, Sea)
          Identification.valuesU(userAnswers) mustBe Seq(
            ImoShipIdNumber,
            SeaGoingVessel
          )
        }
      }

      "must return IataFlightNumber and RegNumberAircraft" - {
        "when border mode of transport is Air" in {
          val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, Air)
          Identification.valuesU(userAnswers) mustBe Seq(
            IataFlightNumber,
            RegNumberAircraft
          )
        }
      }

      "must return TrainNumber" - {
        "when border mode of transport is ChannelTunnel" in {
          val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, ChannelTunnel)
          Identification.valuesU(userAnswers) mustBe Seq(
            TrainNumber
          )
        }
      }

      "must return RegNumberRoadVehicle" - {
        "when border mode of transport is IrishLandBoundary" in {
          val userAnswers = emptyUserAnswers.setValue(BorderModeOfTransportPage, IrishLandBoundary)
          Identification.valuesU(userAnswers) mustBe Seq(
            RegNumberRoadVehicle
          )
        }
      }
    }
  }
}
