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
import pages.sections.transportMeans.DeparturesSection
import pages.transportMeans.departure.{
  AddDepartureTransportMeansYesNoPage,
  AddIdentificationNumberYesNoPage,
  AddIdentificationTypeYesNoPage,
  AddVehicleCountryYesNoPage,
  IdentificationPage,
  MeansIdentificationNumberPage,
  VehicleCountryPage
}
import play.api.libs.json.{JsArray, Json}

class AddDepartureTransportMeansYesNoPageSpec extends PageBehaviours {

  "AddDepartureTransportMeansYesNoPage" - {

    beRetrievable[Boolean](AddDepartureTransportMeansYesNoPage(departureIndex))

    beSettable[Boolean](AddDepartureTransportMeansYesNoPage(departureIndex))

    beRemovable[Boolean](AddDepartureTransportMeansYesNoPage(departureIndex))

    "cleanup" - {
      "when no selected" - {
        "must remove departure means of transport" in {
          val userAnswers = emptyUserAnswers
            .setValue(DeparturesSection, JsArray(Seq(Json.obj("foo" -> "bar"))))

          val result = userAnswers.setValue(AddDepartureTransportMeansYesNoPage(departureIndex), false)

          result.get(AddIdentificationTypeYesNoPage(departureIndex)) must not be defined
          result.get(AddIdentificationNumberYesNoPage(departureIndex)) must not be defined
          result.get(AddVehicleCountryYesNoPage(departureIndex)) must not be defined
          result.get(IdentificationPage(departureIndex)) must not be defined
          result.get(MeansIdentificationNumberPage(departureIndex)) must not be defined
          result.get(VehicleCountryPage(departureIndex)) must not be defined
        }
      }
    }
  }
}
