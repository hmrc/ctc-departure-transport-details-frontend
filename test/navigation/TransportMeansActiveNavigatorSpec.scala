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

package navigation

import base.SpecBase
import config.PhaseConfig
import generators.Generators
import models._
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.external.OfficesOfTransitSection
import play.api.libs.json.{JsArray, Json}

class TransportMeansActiveNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "TransportMeansActiveNavigator" - {

    "when in NormalMode" - {

      val mode = NormalMode

      "when answers complete" - {
        "during transition" - {

          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.Transition)
          val navigatorProvider = new TransportMeansActiveNavigatorProviderImpl()(frontendAppConfig, mockPhaseConfig)
          val navigator         = navigatorProvider.apply(mode, activeIndex)

          "must redirect to transport means check your answers" in {
            forAll(arbitraryTransportMeansActiveAnswers(emptyUserAnswers, activeIndex)) {
              answers =>
                navigator
                  .nextPage(answers, None)
                  .mustBe(controllers.transportMeans.routes.TransportMeansCheckYourAnswersController.onPageLoad(answers.lrn, mode))
            }
          }
        }

        "during post-transition" - {

          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)
          val navigatorProvider = new TransportMeansActiveNavigatorProviderImpl()(frontendAppConfig, mockPhaseConfig)
          val navigator         = navigatorProvider.apply(mode, activeIndex)

          "and customs office of transit is present" - {
            "must redirect to active transport check your answers" in {
              val initialAnswers = emptyUserAnswers.setValue(OfficesOfTransitSection, JsArray(Seq(Json.obj("foo" -> "bar"))))
              forAll(arbitraryTransportMeansActiveAnswers(initialAnswers, activeIndex)(mockPhaseConfig)) {
                answers =>
                  navigator
                    .nextPage(answers, None)
                    .mustBe(controllers.transportMeans.active.routes.CheckYourAnswersController.onPageLoad(lrn, mode, index))
              }
            }
          }

          "and customs office of transit is not present" - {
            "must redirect to transport means check your answers" in {
              forAll(arbitraryTransportMeansActiveAnswers(emptyUserAnswers, activeIndex)(mockPhaseConfig)) {
                answers =>
                  navigator
                    .nextPage(answers, None)
                    .mustBe(controllers.transportMeans.routes.TransportMeansCheckYourAnswersController.onPageLoad(answers.lrn, mode))
              }
            }
          }
        }
      }

      "when in CheckMode" - {

        val mode              = CheckMode
        val navigatorProvider = new TransportMeansActiveNavigatorProviderImpl()
        val navigator         = navigatorProvider.apply(mode, activeIndex)

        "when answers complete" - {
          "must redirect to transport means check your answers" in {
            forAll(arbitraryTransportMeansAnswers(emptyUserAnswers)) {
              answers =>
                navigator
                  .nextPage(answers, None)
                  .mustBe(controllers.transportMeans.routes.TransportMeansCheckYourAnswersController.onPageLoad(answers.lrn, mode))
            }
          }
        }
      }
    }
  }
}
