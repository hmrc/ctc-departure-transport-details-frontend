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

package models.journeyDomain.equipment.seals

import base.SpecBase
import models.Index
import models.journeyDomain.equipment.seal.{SealDomain, SealsDomain}
import org.scalacheck.Gen
import pages.equipment.index.AddAnotherSealPage
import pages.equipment.index.seals.IdentificationNumberPage

class SealsDomainSpec extends SpecBase {

  "Seals Domain" - {

    "can be read from user answers" - {
      "when there are seals" in {
        val seal1 = Gen.alphaNumStr.sample.value
        val seal2 = Gen.alphaNumStr.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(IdentificationNumberPage(equipmentIndex, Index(0)), seal1)
          .setValue(IdentificationNumberPage(equipmentIndex, Index(1)), seal2)

        val expectedResult = SealsDomain(
          Seq(
            SealDomain(seal1)(equipmentIndex, Index(0)),
            SealDomain(seal2)(equipmentIndex, Index(1))
          )
        )(equipmentIndex)

        val result = SealsDomain.userAnswersReader(equipmentIndex).apply(Nil).run(userAnswers)

        result.value.value mustEqual expectedResult
        result.value.pages mustEqual Seq(
          IdentificationNumberPage(equipmentIndex, Index(0)),
          IdentificationNumberPage(equipmentIndex, Index(1)),
          AddAnotherSealPage(equipmentIndex)
        )
      }
    }

    "can not be read from user answers" - {
      "when there aren't any seals" in {
        val result = SealsDomain.userAnswersReader(equipmentIndex).apply(Nil).run(emptyUserAnswers)

        result.left.value.page mustEqual IdentificationNumberPage(equipmentIndex, Index(0))
        result.left.value.pages mustEqual Seq(
          IdentificationNumberPage(equipmentIndex, Index(0))
        )
      }
    }
  }
}
