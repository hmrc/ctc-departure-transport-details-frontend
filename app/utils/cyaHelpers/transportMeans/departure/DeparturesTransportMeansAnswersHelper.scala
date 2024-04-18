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

package utils.cyaHelpers.transportMeans.departure

import config.{FrontendAppConfig, PhaseConfig}
import models.journeyDomain.transportMeans.TransportMeansDepartureDomain
import models.{Index, Mode, TransportMeans, UserAnswers}
import pages.sections.transportMeans.DeparturesSection
import pages.transportMeans.departure._
import play.api.i18n.Messages
import utils.cyaHelpers.AnswersHelper
import viewModels.ListItem

class DeparturesTransportMeansAnswersHelper(userAnswers: UserAnswers, mode: Mode)(implicit
  messages: Messages,
  config: FrontendAppConfig,
  phaseConfig: PhaseConfig
) extends AnswersHelper(userAnswers, mode) {

  def listItems: Seq[Either[ListItem, ListItem]] = {
    def nameWhenInProgress(index: Index): Option[String] =
      TransportMeans(index, userAnswers.get(IdentificationPage(index)), userAnswers.get(MeansIdentificationNumberPage(index))).forAddAnotherDisplay

    buildListItems(DeparturesSection) {
      index =>
        buildListItem[TransportMeansDepartureDomain](
          nameWhenComplete = _.asString,
          nameWhenInProgress = nameWhenInProgress(index),
          removeRoute =
            Some(controllers.transportMeans.departure.routes.RemoveDepartureMeansOfTransportYesNoController.onPageLoad(userAnswers.lrn, mode, index))
        )(TransportMeansDepartureDomain.userAnswersReader(index).apply(Nil))
    }
  }

}
