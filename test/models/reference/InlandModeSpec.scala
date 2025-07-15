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

package models.reference

import base.SpecBase
import config.FrontendAppConfig
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.test.Helpers.running

class InlandModeSpec extends SpecBase with ScalaCheckPropertyChecks {

  "InlandMode" - {

    "must serialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val inlandMode = InlandMode(code, description)
          Json.toJson(inlandMode) mustEqual Json.parse(s"""
                                                            |{
                                                            |  "code": "$code",
                                                            |  "description": "$description"
                                                            |}
                                                            |""".stripMargin)
      }
    }

    "must deserialise" - {
      "when phase -6" in {
        running(_.configure("feature-flags.phase-6-enabled" -> true)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]
            forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
              (code, description) =>
                val inlandMode = InlandMode(code, description)
                Json
                  .parse(s"""
                       |{
                       |  "key": "$code",
                       |  "value": "$description"
                       |}
                       |""".stripMargin)
                  .as[InlandMode](InlandMode.reads(config)) mustEqual inlandMode
            }
        }

      }
      "when phase -5" in {
        running(_.configure("feature-flags.phase-6-enabled" -> false)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]
            forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
              (code, description) =>
                val inlandMode = InlandMode(code, description)
                Json
                  .parse(s"""
                       |{
                       |  "code": "$code",
                       |  "description": "$description"
                       |}
                       |""".stripMargin)
                  .as[InlandMode](InlandMode.reads(config)) mustEqual inlandMode
            }
        }

      }

    }
    "must read from mongo" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val inlandMode = InlandMode(code, description)
          Json
            .parse(s"""
                 |{
                 |  "code": "$code",
                 |  "description": "$description"
                 |}
                 |""".stripMargin)
            .as[InlandMode] mustEqual inlandMode
      }
    }

    "must format as string" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val inlandMode = InlandMode(code, description)
          inlandMode.toString mustEqual s"$description"
      }
    }

    "when description contains raw HTML" in {
      val inlandMode = InlandMode("test", "one &amp; two")
      inlandMode.toString mustEqual "one & two"
    }
  }

}
