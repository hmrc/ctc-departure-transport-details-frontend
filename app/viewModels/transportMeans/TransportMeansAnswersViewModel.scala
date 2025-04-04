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

import config.FrontendAppConfig
import models.journeyDomain.transportMeans.TransportMeansActiveDomain
import models.{Index, Mode, UserAnswers}
import play.api.i18n.Messages
import utils.cyaHelpers.transportMeans.TransportMeansCheckYourAnswersHelper
import utils.cyaHelpers.transportMeans.active.ActiveBorderTransportAnswersHelper
import viewModels.Section
import viewModels.transportMeans.departure.AddDepartureTransportMeansYesNoViewModel.*

import javax.inject.Inject

case class TransportMeansAnswersViewModel(sections: Seq[Section])

object TransportMeansAnswersViewModel {

  class TransportMeansAnswersViewModelProvider @Inject() (implicit config: FrontendAppConfig) {

    def apply(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages): TransportMeansAnswersViewModel = {
      val helper = new TransportMeansCheckYourAnswersHelper(userAnswers, mode)

      val inlandModeSection = Section(
        sectionTitle = messages("transportMeans.inlandMode.subheading"),
        rows = Seq(
          helper.addInlandModeYesNo,
          helper.inlandMode
        ).flatten
      )

      val departureMeansSection: Section = {
        val prefix = new AddDepartureTransportMeansYesNoViewModelProvider().apply(userAnswers).prefix
        val row    = helper.addDepartureTransportMeans(prefix).toSeq
        Section(
          sectionTitle = messages("transportMeans.departureMeans.subheading"),
          rows = row ++ helper.departureTransportsMeans,
          addAnotherLink = helper.addOrRemoveDepartureTransportsMeans()
        )
      }

      val borderModeSection = Section(
        sectionTitle = messages("transportMeans.borderMode.subheading"),
        rows = Seq(
          helper.addModeCrossingBorder(),
          helper.modeCrossingBorder
        ).flatten
      )

      val borderMeansSection = {
        val row = helper.addActiveBorderTransportMeans.toSeq
        if (TransportMeansActiveDomain.hasMultiplicity(userAnswers)) {
          Section(
            sectionTitle = messages("transportMeans.borderMeans.subheading"),
            rows = row ++ helper.activeBorderTransportsMeans,
            addAnotherLink = helper.addOrRemoveActiveBorderTransportsMeans()
          )
        } else {
          Section(
            sectionTitle = messages("transportMeans.borderMeans.subheading"),
            rows = row ++ ActiveBorderTransportAnswersHelper.apply(userAnswers, mode, Index(0))
          )
        }
      }

      new TransportMeansAnswersViewModel(Seq(inlandModeSection, departureMeansSection, borderModeSection, borderMeansSection))
    }
  }
}
