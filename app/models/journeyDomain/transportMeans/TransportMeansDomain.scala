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
import models.domain.{GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.JourneyDomainModel
import models.transportMeans.BorderModeOfTransport
import pages.transportMeans.BorderModeOfTransportPage

case class TransportMeansDomain(
  transportMeansDeparture: Option[TransportMeansDepartureDomain],
  borderModeOfTransport: BorderModeOfTransport,
  transportMeansActiveList: TransportMeansActiveListDomain
) extends JourneyDomainModel

object TransportMeansDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[TransportMeansDomain] =
    (
      UserAnswersReader[TransportMeansDepartureDomain].map(Some(_)),
      BorderModeOfTransportPage.reader,
      UserAnswersReader[TransportMeansActiveListDomain]
    ).tupled.map((TransportMeansDomain.apply _).tupled)
}
