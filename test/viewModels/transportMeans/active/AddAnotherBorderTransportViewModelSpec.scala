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

package viewModels.transportMeans.active

import base.SpecBase
import generators.Generators
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.transportMeans.active.AddAnotherBorderTransportViewModel.AddAnotherBorderTransportViewModelProvider
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._

class AddAnotherBorderTransportViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must get list items" - {

    "when there is one incident" in {
      forAll(arbitrary[Mode]) {
        mode =>
          val userAnswers = arbitraryTransportMeansActiveAnswers(emptyUserAnswers, activeIndex).sample.value

          val result = new AddAnotherBorderTransportViewModelProvider()(frontendAppConfig)(userAnswers, mode)
          result.listItems.length mustEqual 1
          result.title mustEqual "You have added 1 border means of transport"
          result.heading mustEqual "You have added 1 border means of transport"
          result.legend mustEqual "Do you want to add another border means of transport?"
          result.hint mustEqual "Only include vehicles that cross into another CTC country. As the EU is one CTC country, you don’t need to provide vehicle changes that stay within the EU.".toText
          result.maxLimitLabel mustEqual "You cannot add any more border means of transport. To add another, you need to remove one first."
      }
    }

    "when there are multiple incidents" in {
      val formatter = java.text.NumberFormat.getIntegerInstance

      forAll(arbitrary[Mode], Gen.choose(2, frontendAppConfig.maxActiveBorderTransports)) {
        (mode, activeBorderTransports) =>
          val userAnswers = (0 until activeBorderTransports).foldLeft(emptyUserAnswers) {
            (acc, i) =>
              arbitraryTransportMeansActiveAnswers(acc, Index(i)).sample.value
          }

          val result = new AddAnotherBorderTransportViewModelProvider()(frontendAppConfig)(userAnswers, mode)
          result.listItems.length mustEqual activeBorderTransports
          result.title mustEqual s"You have added ${formatter.format(activeBorderTransports)} border means of transport"
          result.heading mustEqual s"You have added ${formatter.format(activeBorderTransports)} border means of transport"
          result.legend mustEqual "Do you want to add another border means of transport?"
          result.hint mustEqual "Only include vehicles that cross into another CTC country. As the EU is one CTC country, you don’t need to provide vehicle changes that stay within the EU.".toText
          result.maxLimitLabel mustEqual "You cannot add any more border means of transport. To add another, you need to remove one first."
      }
    }
  }
}
