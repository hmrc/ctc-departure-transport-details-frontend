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

package pages.preRequisites

import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.sections.equipment.EquipmentsSection
import pages.sections.transportMeans.TransportMeansDepartureSection
import play.api.libs.json.{JsArray, Json}

class ContainerIndicatorPageSpec extends PageBehaviours {

  "ContainerIndicatorPage" - {

    beRetrievable[Boolean](ContainerIndicatorPage)

    beSettable[Boolean](ContainerIndicatorPage)

    beRemovable[Boolean](ContainerIndicatorPage)

    "cleanup" - {
      "when answer changes" - {
        "must remove transport equipments and transport departure means section" in {
          forAll(arbitrary[Boolean]) {
            indicator =>
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, indicator)
                .setValue(EquipmentsSection, JsArray(Seq(Json.obj("foo" -> "bar"))))
                .setValue(TransportMeansDepartureSection, Json.obj("foo" -> "bar"))

              val result = userAnswers.setValue(ContainerIndicatorPage, !indicator)

              result.get(EquipmentsSection) must not be defined
              result.get(TransportMeansDepartureSection) must not be defined
          }
        }
      }

      "when answer doesn't change" - {
        "must do nothing" in {
          forAll(arbitrary[Boolean]) {
            indicator =>
              val userAnswers = emptyUserAnswers
                .setValue(ContainerIndicatorPage, indicator)
                .setValue(EquipmentsSection, JsArray(Seq(Json.obj("foo" -> "bar"))))
                .setValue(TransportMeansDepartureSection, Json.obj("foo" -> "bar"))

              val result = userAnswers.setValue(ContainerIndicatorPage, indicator)

              result.get(EquipmentsSection) must be(defined)
              result.get(TransportMeansDepartureSection) must be(defined)

          }
        }
      }
    }
  }
}
