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
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class BorderModeSpec extends SpecBase with ScalaCheckPropertyChecks {

  private val mockFrontendAppConfig = mock[FrontendAppConfig]

  "BorderMode" - {
    "must serialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val borderMode = BorderMode(code, description)
          Json.toJson(borderMode) mustEqual Json.parse(s"""
                                                            |{
                                                            |  "code": "$code",
                                                            |  "description": "$description"
                                                            |}
                                                            |""".stripMargin)
      }
    }

    "must deserialise" - {
      "when phase -6" in {
        when(mockFrontendAppConfig.phase6Enabled).thenReturn(true)
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (code, description) =>
            val borderMode = BorderMode(code, description)
            Json
              .parse(s"""
                       |{
                       |  "key": "$code",
                       |  "value": "$description"
                       |}
                       |""".stripMargin)
              .as[BorderMode](BorderMode.reads(mockFrontendAppConfig)) mustEqual borderMode
        }
      }
      "when phase -5" in {
        when(mockFrontendAppConfig.phase6Enabled).thenReturn(false)
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (code, description) =>
            val borderMode = BorderMode(code, description)
            Json
              .parse(s"""
                       |{
                       |  "code": "$code",
                       |  "description": "$description"
                       |}
                       |""".stripMargin)
              .as[BorderMode](BorderMode.reads(mockFrontendAppConfig)) mustEqual borderMode
        }
      }

    }
    "must read from mongo" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val borderMode = BorderMode(code, description)
          Json
            .parse(s"""
                 |{
                 |  "code": "$code",
                 |  "description": "$description"
                 |}
                 |""".stripMargin)
            .as[BorderMode] mustEqual borderMode
      }
    }

    "must format as string" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val borderMode = BorderMode(code, description)
          borderMode.toString mustEqual s"$description"
      }
    }

    "when description contains raw HTML" in {
      val borderMode = BorderMode("test", "one &amp; two")
      borderMode.toString mustEqual "one & two"
    }
  }

}
