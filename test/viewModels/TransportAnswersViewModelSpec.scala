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

package viewModels

import base.SpecBase
import generators.Generators
import models.CheckMode
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.TransportAnswersViewModel.TransportAnswersViewModelProvider
import viewModels.transportMeans.TransportMeansAnswersViewModel
import viewModels.transportMeans.TransportMeansAnswersViewModel.TransportMeansAnswersViewModelProvider

class TransportAnswersViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mockTransportMeansAnswersViewModelProvider = mock[TransportMeansAnswersViewModelProvider]

  "apply" - {
    "must return all sections" in {
      val mode = CheckMode
      forAll(arbitraryTransportAnswers(emptyUserAnswers)) {
        answers =>
          when(mockTransportMeansAnswersViewModelProvider.apply(any(), any())(any()))
            .thenReturn(TransportMeansAnswersViewModel(Nil))

          val viewModelProvider = new TransportAnswersViewModelProvider(mockTransportMeansAnswersViewModelProvider, frontendAppConfig)
          val sections          = viewModelProvider.apply(answers).sections

          sections.size mustEqual 8

          sections.head.sectionTitle must not be defined
          sections.head.rows must not be empty
          sections.head.addAnotherLink must not be defined

          sections(1).sectionTitle.get mustEqual "Supply chain actors"

          sections(2).sectionTitle.get mustEqual "Authorisation"

          sections(3).sectionTitle.get mustEqual "Carrier details"
          sections(3).addAnotherLink must not be defined

          sections(4: Int).sectionTitle.get mustEqual "Transport equipment"

          sections(5: Int).sectionTitle.get mustEqual "Transport charges"
          sections(5: Int).addAnotherLink must not be defined

          sections(6: Int).sectionTitle.get mustEqual "Additional references"

          sections(7: Int).sectionTitle.get mustEqual "Additional information"

          verify(mockTransportMeansAnswersViewModelProvider).apply(eqTo(answers), eqTo(mode))(any())
      }
    }
  }
}
