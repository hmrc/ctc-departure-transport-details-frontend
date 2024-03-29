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

import models.reference.InlandMode
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.sections.authorisationsAndLimit.AuthorisationsAndLimitSection
import pages.sections.transportMeans.{ActivesSection, DeparturesSection}
import play.api.libs.json.{JsArray, Json}

class InlandModePageSpec extends PageBehaviours {

  "InlandModePage" - {

    beRetrievable[InlandMode](InlandModePage)

    beSettable[InlandMode](InlandModePage)

    beRemovable[InlandMode](InlandModePage)

    "cleanup" - {
      "when answer changes to something that isn't mail" - {
        "must remove departure and authorisationsAndLimit sections" in {
          forAll(arbitrary[InlandMode]) {
            inlandMode =>
              val userAnswers = emptyUserAnswers
                .setValue(InlandModePage, inlandMode)
                .setValue(DeparturesSection, JsArray(Seq(Json.obj("foo" -> "bar"))))
                .setValue(ActivesSection, JsArray(Seq(Json.obj("foo" -> "bar"))))
                .setValue(AuthorisationsAndLimitSection, Json.obj("foo" -> "bar"))

              forAll(arbitrary[InlandMode].filterNot(_.code == "5").filterNot(_ == inlandMode)) {
                differentInlandModeNotMail =>
                  val result = userAnswers.setValue(InlandModePage, differentInlandModeNotMail)

                  result.get(DeparturesSection) must not be defined
                  result.get(AuthorisationsAndLimitSection) must not be defined
                  result.get(ActivesSection) mustBe defined
              }
          }
        }
      }
    }

    "when answer changes to Mail" - {
      "must remove departure, active and authorisationsAndLimit sections" in {
        val mailInlandMode = InlandMode("5", "Mail (active mode of transport unknown)")

        forAll(arbitrary[InlandMode].suchThat(_.code != "5")) {
          inlandMode =>
            val userAnswers = emptyUserAnswers
              .setValue(InlandModePage, inlandMode)
              .setValue(DeparturesSection, JsArray(Seq(Json.obj("foo" -> "bar"))))
              .setValue(ActivesSection, JsArray(Seq(Json.obj("foo" -> "bar"))))
              .setValue(AuthorisationsAndLimitSection, Json.obj("foo" -> "bar"))

            val result = userAnswers.setValue(InlandModePage, mailInlandMode)

            result.get(DeparturesSection) must not be defined
            result.get(AuthorisationsAndLimitSection) must not be defined
            result.get(ActivesSection) must not be defined
        }
      }
    }
  }
}
