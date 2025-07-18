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

package viewModels.equipment

import base.SpecBase
import generators.Generators
import models.{Index, Mode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.equipment.index._
import viewModels.equipment.EquipmentViewModel.EquipmentViewModelProvider

class EquipmentViewModelSpec extends SpecBase with Generators {

  "apply" - {
    "when user answers empty" - {
      "must return empty rows" in {
        val answers           = emptyUserAnswers
        val mode              = arbitrary[Mode].sample.value
        val viewModelProvider = injector.instanceOf[EquipmentViewModelProvider]
        val sections          = viewModelProvider.apply(answers, mode, index).sections

        sections.size mustEqual 2

        sections.head.sectionTitle must not be defined
        sections.head.rows must be(empty)
        sections.head.addAnotherLink must not be defined

        sections(1).sectionTitle.get mustEqual "Seals"
        sections(1).rows must be(empty)
        sections(1).addAnotherLink must not be defined
      }
    }

    "when user answers populated" - {
      val containerId = nonEmptyString.sample.value

      "must return row for each answer" in {

        val numberOfSeals = Gen.choose(1, 10: Int).sample.value

        implicit class TestRichUserAnswers(userAnswers: UserAnswers) {
          def setSealsValues(): UserAnswers =
            (0 until numberOfSeals).foldLeft(userAnswers) {
              (acc, i) =>
                acc.setValue(seals.IdentificationNumberPage(index, Index(i)), nonEmptyString.sample.value)
            }

        }

        val answers = emptyUserAnswers
          .setValue(AddContainerIdentificationNumberYesNoPage(index), true)
          .setValue(ContainerIdentificationNumberPage(index), containerId)
          .setValue(AddSealYesNoPage(index), true)
          .setSealsValues()

        val mode              = arbitrary[Mode].sample.value
        val viewModelProvider = injector.instanceOf[EquipmentViewModelProvider]
        val sections          = viewModelProvider.apply(answers, mode, index).sections

        sections.size mustEqual 2

        sections.head.sectionTitle must not be defined
        sections.head.rows.size mustEqual 2
        sections.head.rows.head.value.value mustEqual "Yes"
        sections.head.rows(1).value.value mustEqual containerId
        sections.head.addAnotherLink must not be defined

        sections(1).sectionTitle.get mustEqual "Seals"
        sections(1).rows.size mustEqual 1 + numberOfSeals
        sections(1).rows.head.value.value mustEqual "Yes"
        sections(1).addAnotherLink must be(defined)

      }
    }
  }
}
