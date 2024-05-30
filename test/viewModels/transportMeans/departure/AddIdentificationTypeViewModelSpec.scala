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

package viewModels.transportMeans.departure

import base.SpecBase
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.transportMeans.departure.AddIdentificationTypeViewModel.AddIdentificationTypeViewModelProvider

class AddIdentificationTypeViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must create view model" - {
    "when addInlandModeYesNo is true" in {
      val viewModelProvider = new AddIdentificationTypeViewModelProvider()
      val result            = viewModelProvider.apply(addInlandModeYesNo = true)

      result.prefix mustBe "transportMeans.departure.addIdentificationTypeYesNo.inlandModeYes"
      result.title mustBe "Do you want to add the type of identification for this vehicle?"
      result.heading mustBe "Do you want to add the type of identification for this vehicle?"
      result.paragraph mustBe None
    }

    "when addInlandModeYesNo is false" in {
      val viewModelProvider = new AddIdentificationTypeViewModelProvider()
      val result            = viewModelProvider.apply(addInlandModeYesNo = false)

      result.prefix mustBe "transportMeans.departure.addIdentificationTypeYesNo.inlandModeNo"
      result.title mustBe "Do you want to add the type of identification for the departure means of transport?"
      result.heading mustBe "Do you want to add the type of identification for the departure means of transport?"
      result.paragraph.value mustBe "This is the means of transport used from the UK office of departure to a UK port or airport."
    }

  }
}
