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
import play.api.libs.json.{JsBoolean, JsNull, Json}

class OptionalBooleanSpec extends SpecBase {

  "OptionalBoolean" - {

    "must serialise" - {
      "when true" in {
        val result = Json.toJson(OptionalBoolean.yes)
        result mustEqual JsBoolean(true)
      }

      "when false" in {
        val result = Json.toJson(OptionalBoolean.no)
        result mustEqual JsBoolean(false)
      }

      "when maybe" in {
        val result = Json.toJson(OptionalBoolean.maybe)
        result mustEqual JsNull
      }
    }

    "must deserialise" - {
      "when true" in {
        val result = JsBoolean(true).as[OptionalBoolean]
        result mustEqual OptionalBoolean.yes
      }

      "when false" in {
        val result = JsBoolean(false).as[OptionalBoolean]
        result mustEqual OptionalBoolean.no
      }

      "when maybe" in {
        val result = JsNull.as[OptionalBoolean]
        result mustEqual OptionalBoolean.maybe
      }
    }
  }

}
