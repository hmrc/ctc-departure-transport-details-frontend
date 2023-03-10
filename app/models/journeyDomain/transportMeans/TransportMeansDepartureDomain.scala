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
import models.domain.{GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.JourneyDomainModel
import models.reference.Nationality
import models.transportMeans.departure.{Identification, InlandMode}
import pages.transportMeans.departure.{IdentificationPage, InlandModePage, MeansIdentificationNumberPage, VehicleCountryPage}

sealed trait TransportMeansDepartureDomain extends JourneyDomainModel

sealed trait TransportMeansDepartureDomainWithIdentification extends TransportMeansDepartureDomain {
  val identification: Identification
  val identificationNumber: String
  val nationality: Nationality
}

object TransportMeansDepartureDomain {

  implicit val userAnswersReader: UserAnswersReader[TransportMeansDepartureDomain] =
    InlandModePage.reader.flatMap {
      case InlandMode.Mail    => UserAnswersReader.fail(InlandModePage)
      case InlandMode.Unknown => UserAnswersReader[TransportMeansDepartureDomainWithUnknownInlandMode].widen[TransportMeansDepartureDomain]
      case _                  => UserAnswersReader[TransportMeansDepartureDomainWithOtherInlandMode].widen[TransportMeansDepartureDomain]
    }
}

case class TransportMeansDepartureDomainWithUnknownInlandMode(
  identificationNumber: String,
  nationality: Nationality
) extends TransportMeansDepartureDomainWithIdentification {
  override val identification: Identification = Identification.Unknown
}

object TransportMeansDepartureDomainWithUnknownInlandMode {

  implicit val userAnswersReader: UserAnswersReader[TransportMeansDepartureDomainWithUnknownInlandMode] =
    (
      MeansIdentificationNumberPage.reader,
      VehicleCountryPage.reader
    ).tupled.map((TransportMeansDepartureDomainWithUnknownInlandMode.apply _).tupled)
}

case class TransportMeansDepartureDomainWithOtherInlandMode(
  identification: Identification,
  identificationNumber: String,
  nationality: Nationality
) extends TransportMeansDepartureDomainWithIdentification

object TransportMeansDepartureDomainWithOtherInlandMode {

  implicit val userAnswersReader: UserAnswersReader[TransportMeansDepartureDomainWithOtherInlandMode] =
    (
      IdentificationPage.reader,
      MeansIdentificationNumberPage.reader,
      VehicleCountryPage.reader
    ).tupled.map((TransportMeansDepartureDomainWithOtherInlandMode.apply _).tupled)
}
