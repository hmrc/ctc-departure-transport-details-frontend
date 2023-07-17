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

import pages.behaviours.PageBehaviours
import pages.sections.transportMeans.TransportMeansActiveListSection
import play.api.libs.json.{JsArray, Json}

class AddActiveBorderTransportMeansYesNoPageSpec extends PageBehaviours {

  "AddActiveBorderTransportMeansYesNoPage" - {

    beRetrievable[Boolean](AddActiveBorderTransportMeansYesNoPage)

    beSettable[Boolean](AddActiveBorderTransportMeansYesNoPage)

    beRemovable[Boolean](AddActiveBorderTransportMeansYesNoPage)

    "cleanup" - {
      "when no selected" - {
        "must remove active border means of transport" in {
          val userAnswers = emptyUserAnswers
            .setValue(TransportMeansActiveListSection, JsArray(Seq(Json.obj("foo" -> "bar"))))

          val result = userAnswers.setValue(AddActiveBorderTransportMeansYesNoPage, false)

          result.get(TransportMeansActiveListSection) must not be defined
        }
      }
    }
  }
}
