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

package models.journeyDomain.supplyChainActors

import base.SpecBase
import generators.Generators
import models.reference.supplyChainActors.SupplyChainActorType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.supplyChainActors.index.{IdentificationNumberPage, SupplyChainActorTypePage}

class SupplyChainActorDomainSpec extends SpecBase with Generators {

  "SupplyChainActorDomain" - {

    val role                 = arbitrary[SupplyChainActorType].sample.value
    val identificationNumber = Gen.alphaNumStr.sample.value

    "can be parsed from UserAnswers" - {

      "when answers provided" in {
        val userAnswers = emptyUserAnswers
          .setValue(SupplyChainActorTypePage(actorIndex), role)
          .setValue(IdentificationNumberPage(actorIndex), identificationNumber)

        val expectedResult = SupplyChainActorDomain(
          role = role,
          identification = identificationNumber
        )(index)

        val result = SupplyChainActorDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

        result.value.value mustEqual expectedResult
        result.value.pages mustEqual Seq(
          SupplyChainActorTypePage(actorIndex),
          IdentificationNumberPage(actorIndex)
        )
      }
    }

    "cannot be parsed from user answers" - {
      "when type missing" in {
        val result = SupplyChainActorDomain.userAnswersReader(index).apply(Nil).run(emptyUserAnswers)

        result.left.value.page mustEqual SupplyChainActorTypePage(index)
        result.left.value.pages mustEqual Seq(
          SupplyChainActorTypePage(actorIndex)
        )
      }

      "when id number missing" in {
        val userAnswers = emptyUserAnswers
          .setValue(SupplyChainActorTypePage(actorIndex), role)

        val result = SupplyChainActorDomain.userAnswersReader(index).apply(Nil).run(userAnswers)

        result.left.value.page mustEqual IdentificationNumberPage(index)
        result.left.value.pages mustEqual Seq(
          SupplyChainActorTypePage(actorIndex),
          IdentificationNumberPage(actorIndex)
        )
      }
    }
  }
}
