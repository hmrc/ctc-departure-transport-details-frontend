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

import cats.implicits._
import config.PhaseConfig
import models.Phase
import models.domain.{GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.JourneyDomainModel
import models.reference.Nationality
import models.transportMeans.departure.Identification
import pages.transportMeans.departure._

sealed trait TransportMeansDepartureDomain extends JourneyDomainModel

object TransportMeansDepartureDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportMeansDepartureDomain] =
    phaseConfig.phase match {
      case Phase.Transition =>
        ???
      case Phase.PostTransition =>
        PostTransitionTransportMeansDepartureDomain.userAnswersReader.widen[TransportMeansDepartureDomain]
    }
}

case class PostTransitionTransportMeansDepartureDomain(
  identification: Identification,
  identificationNumber: String,
  nationality: Nationality
) extends TransportMeansDepartureDomain

object PostTransitionTransportMeansDepartureDomain {

  implicit val userAnswersReader: UserAnswersReader[PostTransitionTransportMeansDepartureDomain] =
    (
      IdentificationPage.reader,
      MeansIdentificationNumberPage.reader,
      VehicleCountryPage.reader
    ).tupled.map((PostTransitionTransportMeansDepartureDomain.apply _).tupled)
}
