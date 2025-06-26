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

package models.reference.transportMeans.departure

import base.SpecBase
import config.FrontendAppConfig
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.test.Helpers.running

class IdentificationSpec extends SpecBase with ScalaCheckPropertyChecks {

  "Identification" - {

    "must serialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val identification = Identification(code, description)
          Json.toJson(identification) mustBe Json.parse(s"""
                                                            |{
                                                            |  "type": "$code",
                                                            |  "description": "$description"
                                                            |}
                                                            |""".stripMargin)
      }
    }

    "must deserialise" - {
      "when phase-6" in {
        running(_.configure("feature-flags.phase-6-enabled" -> true)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]
            forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
              (code, description) =>
                val identification = Identification(code, description)
                Json
                  .parse(s"""
                       |{
                       |  "key": "$code",
                       |  "value": "$description"
                       |}
                       |""".stripMargin)
                  .as[Identification](Identification.reads(config)) mustEqual identification
            }

        }

      }
      "when phase-5" in {
        running(_.configure("feature-flags.phase-6-enabled" -> false)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]
            forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
              (code, description) =>
                val identification = Identification(code, description)
                Json
                  .parse(s"""
                       |{
                       |  "type": "$code",
                       |  "description": "$description"
                       |}
                       |""".stripMargin)
                  .as[Identification](Identification.reads(config)) mustEqual identification
            }

        }

      }
    }
    "when read from mongo" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val identification = Identification(code, description)
          Json
            .parse(s"""
                 |{
                 | "type": "$code",
                 | "description": "$description"
                 |}
                 |""".stripMargin)
            .as[Identification] mustEqual identification
      }
    }

    "must format as string" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val identification = Identification(code, description)
          identification.toString mustBe s"$description"
      }
    }

    "when description contains raw HTML" in {
      val identification = Identification("test", "one &amp; two")
      identification.toString mustBe "one & two"
    }
  }

}
