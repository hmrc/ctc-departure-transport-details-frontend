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

package models.journeyDomain.additionalInformation

import models.journeyDomain.{JourneyDomainModel, JsArrayGettableAsReaderOps, Read}
import models.{Index, RichJsArray, UserAnswers}
import pages.sections.Section
import pages.sections.additionalInformation.AdditionalInformationListSection
import models.journeyDomain.RichRead

case class AdditionalInformationsDomain(
  value: Seq[AdditionalInformationDomain]
) extends JourneyDomainModel {

  override def page(userAnswers: UserAnswers): Option[Section[?]] = Some(AdditionalInformationListSection)
}

object AdditionalInformationsDomain {

  def userAnswersReader: Read[AdditionalInformationsDomain] = {
    val additionalInformationsReader: Read[Seq[AdditionalInformationDomain]] =
      AdditionalInformationListSection.arrayReader.to {
        case arr if arr.isEmpty =>
          AdditionalInformationDomain.userAnswersReader(Index(0)).toSeq
        case arr =>
          arr.traverse[AdditionalInformationDomain](AdditionalInformationDomain.userAnswersReader(_).apply(_))
      }

    additionalInformationsReader.map(AdditionalInformationsDomain.apply)
  }
}
