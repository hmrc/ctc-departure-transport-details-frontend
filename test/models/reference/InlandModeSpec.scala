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
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class InlandModeSpec extends SpecBase with ScalaCheckPropertyChecks {

  "InlandMode" - {

    "must serialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val inlandMode = InlandMode(code, description)
          Json.toJson(inlandMode) mustBe Json.parse(s"""
                                                            |{
                                                            |  "code": "$code",
                                                            |  "description": "$description"
                                                            |}
                                                            |""".stripMargin)
      }
    }

    "must deserialise" in {
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
            .as[InlandMode] mustBe inlandMode
      }
    }

    "must format as string" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val inlandMode = InlandMode(code, description)
          inlandMode.toString mustBe s"$description"
      }
    }

    "when description contains raw HTML" in {
      val inlandMode = InlandMode("3", "one &amp; two")
      inlandMode.toString mustBe "one & two"
    }
  }

}
