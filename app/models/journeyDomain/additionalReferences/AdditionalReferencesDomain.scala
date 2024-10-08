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

import models.journeyDomain.{JourneyDomainModel, JsArrayGettableAsReaderOps, Read}
import models.{Index, RichJsArray, UserAnswers}
import pages.sections.Section
import pages.sections.additionalReference.AdditionalReferencesSection
import models.journeyDomain.RichRead

case class AdditionalReferencesDomain(
  value: Seq[AdditionalReferenceDomain]
) extends JourneyDomainModel {

  override def page(userAnswers: UserAnswers): Option[Section[?]] = Some(AdditionalReferencesSection)
}

object AdditionalReferencesDomain {

  def userAnswersReader: Read[AdditionalReferencesDomain] = {
    val additionalReferencesReader: Read[Seq[AdditionalReferenceDomain]] =
      AdditionalReferencesSection.arrayReader.to {
        case x if x.isEmpty =>
          AdditionalReferenceDomain.userAnswersReader(Index(0)).toSeq
        case x =>
          x.traverse[AdditionalReferenceDomain](AdditionalReferenceDomain.userAnswersReader(_).apply(_))
      }

    additionalReferencesReader.map(AdditionalReferencesDomain.apply)
  }
}
