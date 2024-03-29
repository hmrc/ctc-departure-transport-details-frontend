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

package pages.transportMeans

import models.Index
import models.reference.BorderMode
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.sections.transportMeans.ActiveSection
import play.api.libs.json.Json

class BorderModeOfTransportPageSpec extends PageBehaviours {

  "BorderModeOfTransportPage" - {

    beRetrievable[BorderMode](BorderModeOfTransportPage)

    beSettable[BorderMode](BorderModeOfTransportPage)

    beRemovable[BorderMode](BorderModeOfTransportPage)
  }

  "cleanup" - {
    "when answer changes" - {
      "must remove identification" in {
        forAll(arbitrary[BorderMode]) {
          borderMode =>
            val userAnswers = emptyUserAnswers
              .setValue(BorderModeOfTransportPage, borderMode)
              .setValue(ActiveSection(Index(0)), Json.obj("foo" -> "bar"))

            forAll(arbitrary[BorderMode].retryUntil(_ != borderMode)) {
              differentBorderMode =>
                val result = userAnswers.setValue(BorderModeOfTransportPage, differentBorderMode)

                result.get(ActiveSection(Index(0))) must not be defined
            }
        }
      }
    }
  }
}
