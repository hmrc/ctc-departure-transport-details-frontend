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

import config.PhaseConfig
import models.journeyDomain.{JourneyDomainModel, _}
import models.{Index, RichJsArray, UserAnswers}
import pages.sections.Section
import pages.sections.transportMeans.DeparturesSection

case class TransportMeansDepartureListDomain(
  transportMeansDepartureListDomain: Seq[TransportMeansDepartureDomain]
) extends JourneyDomainModel {

  override def page(userAnswers: UserAnswers): Option[Section[_]] =
    None // TODO - change to Some(DeparturesSection) when 'add another' page built
}

object TransportMeansDepartureListDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[TransportMeansDepartureListDomain] = {

    val departuresReader: Read[Seq[TransportMeansDepartureDomain]] =
      DeparturesSection.arrayReader.to {
        case x if x.isEmpty =>
          TransportMeansDepartureDomain.userAnswersReader(Index(0)).toSeq
        case x =>
          x.traverse[TransportMeansDepartureDomain](TransportMeansDepartureDomain.userAnswersReader(_).apply(_))
      }

    departuresReader.map(TransportMeansDepartureListDomain.apply)
  }
}
