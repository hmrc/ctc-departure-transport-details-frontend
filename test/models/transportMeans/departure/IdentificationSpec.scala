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

package models.transportMeans.departure

import base.SpecBase
import generators.Generators
import models.reference.InlandMode
import models.transportMeans.departure.Identification._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.InlandModePage
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

    "Radio options" - {

      val maritimeInlandMode = InlandMode("1", "Maritime Transport")
      val railInlandMode     = InlandMode("2", "Rail Transport")
      val roadInlandMode     = InlandMode("3", "Road transport")
      val airInlandMode      = InlandMode("4", "Air transport")
      val fixedInlandMode    = InlandMode("7", "Fixed transport installations")
      val waterwayInlandMode = InlandMode("8", "Inland waterway transport")

      "Must return the correct number of radios" - {
        "When InlandMode is 'Maritime'" in {
          val answers = emptyUserAnswers
            .setValue(InlandModePage, maritimeInlandMode)

          val radios = Identification.values(answers)
          val expected = Seq(
            Identification.SeaGoingVessel,
            Identification.ImoShipIdNumber
          )

          radios mustBe expected
        }

        "When InlandMode is 'Rail'" in {
          val answers = emptyUserAnswers
            .setValue(InlandModePage, railInlandMode)

          val radios = Identification.values(answers)
          val expected = Seq(
            Identification.WagonNumber,
            Identification.TrainNumber
          )

          radios mustBe expected
        }

        "When InlandMode is 'Road'" in {
          val answers = emptyUserAnswers
            .setValue(InlandModePage, roadInlandMode)

          val radios = Identification.values(answers)
          val expected = Seq(
            Identification.RegNumberRoadVehicle,
            Identification.RegNumberRoadTrailer
          )

          radios mustBe expected
        }

        "When InlandMode is 'Air'" in {
          val answers = emptyUserAnswers
            .setValue(InlandModePage, airInlandMode)

          val radios = Identification.values(answers)
          val expected = Seq(
            Identification.IataFlightNumber,
            Identification.RegNumberAircraft
          )

          radios mustBe expected
        }

        "When InlandMode is 'Fixed'" in {
          val answers = emptyUserAnswers
            .setValue(InlandModePage, fixedInlandMode)

          val radios = Identification.values(answers)
          val expected = Seq(
            Identification.SeaGoingVessel,
            Identification.IataFlightNumber,
            Identification.InlandWaterwaysVehicle,
            Identification.ImoShipIdNumber,
            Identification.WagonNumber,
            Identification.TrainNumber,
            Identification.RegNumberRoadVehicle,
            Identification.RegNumberRoadTrailer,
            Identification.RegNumberAircraft,
            Identification.EuropeanVesselIdNumber
          )

          radios mustBe expected
        }

        "When InlandMode is 'Waterway'" in {
          val answers = emptyUserAnswers
            .setValue(InlandModePage, waterwayInlandMode)

          val radios = Identification.values(answers)
          val expected = Seq(
            Identification.InlandWaterwaysVehicle,
            Identification.EuropeanVesselIdNumber
          )

          radios mustBe expected
        }
      }
    }
  }
}
