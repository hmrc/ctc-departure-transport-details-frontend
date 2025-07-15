/*
 * Copyright 2024 HM Revenue & Customs
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
import config.Constants.ModeOfTransport.Road
import generators.Generators
import models.Index
import models.reference.InlandMode
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportMeans.InlandModePage
import viewModels.transportMeans.departure.IdentificationViewModel.IdentificationViewModelProvider

class IdentificationViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must create view model" - {
    "when InlandMode mode is not ROAD" in {
      val viewModelProvider = new IdentificationViewModelProvider()
      val result            = viewModelProvider.apply(emptyUserAnswers, departureIndex)

      result.para must not be defined
    }

    "when InlandMode is Road for first Index" in {
      val viewModelProvider = new IdentificationViewModelProvider()

      val userAnswers = emptyUserAnswers.setValue(InlandModePage, InlandMode(Road, "_"))

      val result = viewModelProvider.apply(userAnswers, departureIndex)

      result.para.value mustBe "You must add the registration number of the road vehicle for your inland mode."
    }

    "when InlandMode is Road and not first Index" in {
      val viewModelProvider = new IdentificationViewModelProvider()

      val userAnswers = emptyUserAnswers.setValue(InlandModePage, InlandMode(Road, "_"))

      val result = viewModelProvider.apply(userAnswers, Index(1))

      result.para.value mustBe "You must add the registration number of the road trailer for your inland mode."
    }
  }
}
