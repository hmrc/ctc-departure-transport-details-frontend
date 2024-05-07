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

package models.journeyDomain.additionalReferences

import models.journeyDomain.Stage.{AccessingJourney, CompletingJourney}
import models.journeyDomain.{GettableAsReaderOps, JourneyDomainModel, Read, Stage, UserAnswersReader}
import models.reference.additionalReference.AdditionalReferenceType
import models.{Index, Mode, Phase, UserAnswers}
import pages.additionalReference.index.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import pages.sections.additionalReference.AdditionalReferencesSection
import play.api.mvc.Call

case class AdditionalReferenceDomain(
  `type`: AdditionalReferenceType,
  number: Option[String]
)(additionalReferenceIndex: Index)
    extends JourneyDomainModel {

  override def toString: String = AdditionalReferenceDomain.asString(`type`, number)

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage, phase: Phase): Option[Call] = Some {
    stage match {
      case AccessingJourney =>
        controllers.additionalReference.index.routes.AdditionalReferenceTypeController.onPageLoad(userAnswers.lrn, mode, additionalReferenceIndex)
      case CompletingJourney =>
        Call("GET", "#") //TODO go to addAnother controller
    }
  }
}

object AdditionalReferenceDomain {

  def asString(`type`: AdditionalReferenceType, number: Option[String]): String = `type`.toString + number.fold("") {
    value => s" - $value"
  }

  def userAnswersReader(index: Index): Read[AdditionalReferenceDomain] =
    (
      AdditionalReferenceTypePage(index).reader,
      AdditionalReferenceNumberPage(index).optionalReader
    ).map(AdditionalReferenceDomain.apply(_, _)(index))
}
