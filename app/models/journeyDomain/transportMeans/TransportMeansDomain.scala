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

import config.Constants.SecurityType.NoSecurityDetails
import models.journeyDomain.transportMeans.TransportMeansDomain.{borderModeOfTransportReader, transportMeansActiveReader}
import models.journeyDomain.{JourneyDomainModel, *}
import models.reference.BorderMode
import models.{Index, UserAnswers}
import pages.external.SecurityDetailsTypePage
import pages.sections.Section
import pages.sections.transportMeans.TransportMeansSection
import pages.transportMeans.*

sealed trait TransportMeansDomain extends JourneyDomainModel {

  override def page(userAnswers: UserAnswers): Option[Section[?]] = Some(TransportMeansSection)
}

object TransportMeansDomain {

  implicit val userAnswersReader: Read[TransportMeansDomain] =
    UserAnswersReader
      .success {
        ua => TransportMeansActiveDomain.hasMultiplicity(ua)
      }
      .to {
        case true =>
          TransportMeansMultipleActiveDomain.userAnswersReader
        case false =>
          TransportMeansSingleActiveDomain.userAnswersReader
      }

  lazy val borderModeOfTransportReader: Read[Option[BorderMode]] =
    SecurityDetailsTypePage.reader.to {
      case NoSecurityDetails =>
        AddBorderModeOfTransportYesNoPage.filterOptionalDependent(identity)(BorderModeOfTransportPage.reader)
      case _ =>
        BorderModeOfTransportPage.reader.toOption
    }

  def transportMeansActiveReader[T](read: Read[T]): Read[Option[T]] =
    SecurityDetailsTypePage.reader.to {
      case NoSecurityDetails => AddActiveBorderTransportMeansYesNoPage.filterOptionalDependent(identity)(read)
      case _                 => read.toOption
    }

}

case class TransportMeansMultipleActiveDomain(
  transportMeansDepartureList: TransportMeansDepartureListDomain,
  borderModeOfTransport: Option[BorderMode],
  transportMeansActiveList: Option[TransportMeansActiveListDomain]
) extends TransportMeansDomain

object TransportMeansMultipleActiveDomain {

  implicit val userAnswersReader: Read[TransportMeansDomain] =
    (
      TransportMeansDepartureListDomain.userAnswersReader,
      borderModeOfTransportReader,
      transportMeansActiveReader(TransportMeansActiveListDomain.userAnswersReader)
    ).map(TransportMeansMultipleActiveDomain.apply)
}

case class TransportMeansSingleActiveDomain(
  transportMeansDepartureList: TransportMeansDepartureListDomain,
  borderModeOfTransport: Option[BorderMode],
  transportMeansActiveList: Option[TransportMeansActiveDomain]
) extends TransportMeansDomain

object TransportMeansSingleActiveDomain {

  implicit val userAnswersReader: Read[TransportMeansDomain] =
    (
      TransportMeansDepartureListDomain.userAnswersReader,
      borderModeOfTransportReader,
      transportMeansActiveReader(TransportMeansActiveDomain.userAnswersReader(Index(0)))
    ).map(TransportMeansSingleActiveDomain.apply)
}
