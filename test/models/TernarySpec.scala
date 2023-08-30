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

package models

import base.SpecBase
import generators.Generators
import models.Ternary._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class TernarySpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Ternary" - {

    "must deserialise valid values" in {
      forAll(arbitrary[Ternary]) {
        ternary =>
          JsString(ternary.toString).validate[Ternary].asOpt.value mustEqual ternary
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] retryUntil (!Ternary.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[Ternary] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {
      forAll(arbitrary[Ternary]) {
        ternary =>
          Json.toJson(ternary) mustEqual JsString(ternary.toString)
      }
    }

    "values" - {
      "must return Yes and No" - {
        "when standard declaration" in {
          val result = Ternary.values("A")
          result mustBe Seq(True, False)
        }
      }

      "must return all values" - {
        "when pre-lodge declaration" in {
          val result = Ternary.values("D")
          result mustBe Seq(True, False, Maybe)
        }
      }
    }
  }
}
