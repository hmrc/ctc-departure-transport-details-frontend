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

package models.journeyDomain.transportMeans

import models.journeyDomain._
import models.journeyDomain.JourneyDomainModel
import models.{Index, RichJsArray, UserAnswers}
import pages.sections.Section
import pages.sections.transportMeans.ActivesSection

case class TransportMeansActiveListDomain(
  transportMeansActiveListDomain: Seq[TransportMeansActiveDomain]
) extends JourneyDomainModel {

  override def page(userAnswers: UserAnswers): Option[Section[?]] = Some(ActivesSection)
}

object TransportMeansActiveListDomain {

  implicit val userAnswersReader: Read[TransportMeansActiveListDomain] = {

    val activesReader: Read[Seq[TransportMeansActiveDomain]] =
      ActivesSection.arrayReader.to {
        case x if x.isEmpty =>
          TransportMeansActiveDomain.userAnswersReader(Index(0)).toSeq
        case x =>
          x.traverse[TransportMeansActiveDomain](TransportMeansActiveDomain.userAnswersReader(_).apply(_))
      }

    activesReader.map(TransportMeansActiveListDomain.apply)
  }
}
