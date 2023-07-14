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

package viewModels.transportMeans

import config.{FrontendAppConfig, PhaseConfig}
import models.{Index, Mode, Phase, UserAnswers}
import pages.sections.external.OfficesOfTransitSection
import play.api.i18n.Messages
import utils.cyaHelpers.transportMeans.TransportMeansCheckYourAnswersHelper
import utils.cyaHelpers.transportMeans.active.ActiveBorderTransportAnswersHelper
import viewModels.Section

import javax.inject.Inject

case class TransportMeansAnswersViewModel(sections: Seq[Section])

object TransportMeansAnswersViewModel {

  class TransportMeansAnswersViewModelProvider @Inject() (implicit config: FrontendAppConfig) {

    def apply(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages, phaseConfig: PhaseConfig): TransportMeansAnswersViewModel = {
      val helper = new TransportMeansCheckYourAnswersHelper(userAnswers, mode)

      val inlandModeSection = Section(
        sectionTitle = messages("transportMeans.inlandMode.subheading"),
        rows = Seq(helper.inlandMode).flatten
      )

      val departureMeansSection = Section(
        sectionTitle = messages("transportMeans.departureMeans.subheading"),
        rows = Seq(
          helper.departureIdentificationType,
          helper.departureIdentificationNumber,
          helper.departureNationality
        ).flatten
      )

      val borderModeSection = Section(
        sectionTitle = messages("transportMeans.borderMode.subheading"),
        rows = Seq(helper.modeCrossingBorder).flatten
      )

      val borderMeansSection =
        phaseConfig.phase match {
          case Phase.PostTransition =>
            if (userAnswers.get(OfficesOfTransitSection).isDefined) {
              Section(
                sectionTitle = messages("transportMeans.borderMeans.subheading"),
                rows = helper.activeBorderTransportsMeans,
                addAnotherLink = helper.addOrRemoveActiveBorderTransportsMeans()
              )
            } else {
              Section(
                sectionTitle = messages("transportMeans.borderMeans.subheading"),
                rows = ActiveBorderTransportAnswersHelper.apply(userAnswers, mode, Index(0))
              )
            }

          case Phase.Transition =>
            val helper = new ActiveBorderTransportAnswersHelper(userAnswers, mode, Index(0))

            Section(
              sectionTitle = messages("transportMeans.borderMeans.subheading"),
              rows = Seq(
                helper.activeBorderIdentificationType,
                helper.activeBorderIdentificationNumber,
                helper.activeBorderAddNationality,
                helper.activeBorderNationality,
                helper.customsOfficeAtBorder,
                helper.activeBorderConveyanceReferenceNumberYesNo,
                helper.conveyanceReferenceNumber
              ).flatten
            )
        }

      new TransportMeansAnswersViewModel(Seq(inlandModeSection, departureMeansSection, borderModeSection, borderMeansSection))
    }
  }
}
