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

package models.journeyDomain.equipment

import base.SpecBase
import generators.{Generators, UserAnswersGenerator}
import models.Index
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.equipment.AddAnotherEquipmentPage

class EquipmentsDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators with UserAnswersGenerator {

  "Equipments Domain" - {

    "can be read from user answers" - {
      "when there are equipments" in {
        val numberOfEquipments = Gen.choose(1, 5: Int).sample.value

        val userAnswers = (0 until numberOfEquipments).foldLeft(emptyUserAnswers) {
          (acc, i) =>
            arbitraryEquipmentAnswers(acc, Index(i)).sample.value
        }

        val result = EquipmentsDomain.userAnswersReader.apply(Nil).run(userAnswers)

        result.value.value.value.size mustEqual numberOfEquipments
        result.value.pages.last mustEqual AddAnotherEquipmentPage
      }
    }

    "can not be read from user answers" - {
      "when there aren't any equipments" in {
        val result = EquipmentsDomain.userAnswersReader.apply(Nil).run(emptyUserAnswers)

        result.isLeft mustEqual true
      }
    }
  }
}
