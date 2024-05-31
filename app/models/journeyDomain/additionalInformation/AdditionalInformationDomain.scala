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

package models.journeyDomain.additionalInformation

import models.journeyDomain.Stage.{AccessingJourney, CompletingJourney}
import models.journeyDomain.{GettableAsFilterForNextReaderOps, GettableAsReaderOps, JourneyDomainModel, Read, Stage}
import models.reference.additionalInformation.AdditionalInformationCode
import models.{Index, Mode, Phase, UserAnswers}
import pages.additionalInformation.AddCommentsYesNoPage
import pages.additionalInformation.index.{AdditionalInformationTextPage, AdditionalInformationTypePage}
import play.api.mvc.Call

case class AdditionalInformationDomain(
  `type`: AdditionalInformationCode,
  value: Option[String]
)(additionalInformationIndex: Index)
    extends JourneyDomainModel {

  override def toString: String = AdditionalInformationDomain.asString(`type`, value)

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] = Some {
    stage match {
      case AccessingJourney =>
        controllers.additionalInformation.index.routes.AdditionalInformationTypeController.onPageLoad(additionalInformationIndex, userAnswers.lrn, mode)
      case CompletingJourney =>
        controllers.additionalInformation.routes.AddAnotherAdditionalInformationController.onPageLoad(userAnswers.lrn, mode)
    }
  }
}

object AdditionalInformationDomain {

  def asString(`type`: AdditionalInformationCode, text: Option[String]): String = `type`.toString + text.fold("") {
    value => s" - $value"
  }

  def userAnswersReader(additionalInformationIndex: Index): Read[AdditionalInformationDomain] = (
    AdditionalInformationTypePage(additionalInformationIndex).reader,
    AddCommentsYesNoPage(additionalInformationIndex).filterOptionalDependent(identity) {
      AdditionalInformationTextPage(additionalInformationIndex).reader
    }
  ).map(AdditionalInformationDomain.apply(_, _)(additionalInformationIndex))
}
