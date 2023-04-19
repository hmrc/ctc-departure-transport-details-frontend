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
import models.reference.CustomsOffice
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class SelectableListSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "officesOfExitReads" - {
    "must read offices of exit as SelectableList" in {
      val json = Json.parse("""
          |[
          |  {
          |    "officeOfExit": {
          |      "id": "GB1",
          |      "name": "Newcastle"
          |    }
          |  },
          |  {
          |    "officeOfExit": {
          |      "id": "GB2",
          |      "name": "London",
          |      "phoneNumber": "999"
          |    }
          |  }
          |]
          |""".stripMargin)

      val result = json.as[SelectableList[CustomsOffice]](SelectableList.officesOfExitReads)

      result mustBe SelectableList(
        Seq(
          CustomsOffice("GB1", "Newcastle", None),
          CustomsOffice("GB2", "London", Some("999"))
        )
      )
    }
  }

  "officesOfTransitReads" - {
    "must read offices of transit as SelectableList" in {
      val json = Json.parse("""
          |[
          |  {
          |    "officeOfTransit": {
          |      "id": "GB1",
          |      "name": "Newcastle"
          |    }
          |  },
          |  {
          |    "officeOfTransit": {
          |      "id": "GB2",
          |      "name": "London",
          |      "phoneNumber": "999"
          |    }
          |  }
          |]
          |""".stripMargin)

      val result = json.as[SelectableList[CustomsOffice]](SelectableList.officesOfTransitReads)

      result mustBe SelectableList(
        Seq(
          CustomsOffice("GB1", "Newcastle", None),
          CustomsOffice("GB2", "London", Some("999"))
        )
      )
    }
  }
}
