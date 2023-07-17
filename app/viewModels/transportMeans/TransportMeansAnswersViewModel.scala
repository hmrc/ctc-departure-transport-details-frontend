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
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
import models.{Index, Mode, Phase, UserAnswers}
=======
import models.{Index, Mode, UserAnswers}
>>>>>>> 7c2ee49... CTCP-3468: Add phaseConfig implicits
=======
import models.{Index, Mode, Phase, UserAnswers}
>>>>>>> 085b1a1... CTCP-3468: Add transition logic for section cya
import pages.sections.external.OfficesOfTransitSection
=======
import models.journeyDomain.transportMeans.TransportMeansActiveDomain
import models.{Index, Mode, UserAnswers}
>>>>>>> 0db6706... Extracting logic out.
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
          helper.addDepartureTransportMeans,
          helper.departureIdentificationType,
          helper.departureIdentificationNumber,
          helper.departureNationality
        ).flatten
      )

      val borderModeSection = Section(
        sectionTitle = messages("transportMeans.borderMode.subheading"),
        rows = Seq(
          helper.addModeCrossingBorder(),
          helper.modeCrossingBorder
        ).flatten
      )

<<<<<<< HEAD
<<<<<<< HEAD
      val borderMeansSection =
        phaseConfig.phase match {
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> d659ba1... CTCP-3468: Remove duplication from TransportMeansAnswersViewModel
          case Phase.PostTransition if userAnswers.get(OfficesOfTransitSection).isDefined =>
            Section(
              sectionTitle = messages("transportMeans.borderMeans.subheading"),
              rows = helper.activeBorderTransportsMeans,
              addAnotherLink = helper.addOrRemoveActiveBorderTransportsMeans()
            )
          case _ =>
<<<<<<< HEAD
            Section(
              sectionTitle = messages("transportMeans.borderMeans.subheading"),
              rows = ActiveBorderTransportAnswersHelper.apply(userAnswers, mode, Index(0))
=======
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
=======
>>>>>>> d659ba1... CTCP-3468: Remove duplication from TransportMeansAnswersViewModel
            Section(
              sectionTitle = messages("transportMeans.borderMeans.subheading"),
<<<<<<< HEAD
              rows = Seq(
                helper.activeBorderIdentificationType,
                helper.activeBorderIdentificationNumber,
                helper.activeBorderAddNationality,
                helper.activeBorderNationality,
                helper.customsOfficeAtBorder,
                helper.activeBorderConveyanceReferenceNumberYesNo,
                helper.conveyanceReferenceNumber
              ).flatten
>>>>>>> 085b1a1... CTCP-3468: Add transition logic for section cya
=======
              rows = ActiveBorderTransportAnswersHelper.apply(userAnswers, mode, Index(0))
>>>>>>> a149937... CTCP-3468: Add tests for nav & viewmodel transition/post-transition toggle
            )
        }
=======
      val borderMeansSection = if (TransportMeansActiveDomain.hasMultiplicity(userAnswers, phaseConfig.phase)) {
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
=======
      val borderMeansSection = {
        val row = helper.addActiveBorderTransportMeans.toSeq
        if (TransportMeansActiveDomain.hasMultiplicity(userAnswers, phaseConfig.phase)) {
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
>>>>>>> 84d5653... CTCP-3434: Including new questions on CYA.
      }
>>>>>>> 0db6706... Extracting logic out.

      new TransportMeansAnswersViewModel(Seq(inlandModeSection, departureMeansSection, borderModeSection, borderMeansSection))
    }
  }
}
