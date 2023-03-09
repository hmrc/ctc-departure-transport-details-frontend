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
import controllers.transportMeans.routes
import models.SecurityDetailsType.NoSecurityDetails
import models.domain.{GettableAsReaderOps, UserAnswersReader}
import models.journeyDomain.{JourneyDomainModel, Stage}
import models.transportMeans.BorderModeOfTransport
import models.transportMeans.departure.InlandMode
import models.{Mode, UserAnswers}
import pages.external.SecurityDetailsTypePage
import pages.transportMeans.BorderModeOfTransportPage
import pages.transportMeans.departure.InlandModePage
import play.api.mvc.Call

sealed trait TransportMeansDomain extends JourneyDomainModel {
  val inlandMode: InlandMode

  override def routeIfCompleted(userAnswers: UserAnswers, mode: Mode, stage: Stage): Option[Call] =
    Option(routes.TransportMeansCheckYourAnswersController.onPageLoad(userAnswers.lrn, mode))
}

object TransportMeansDomain {

  implicit val userAnswersReader: UserAnswersReader[TransportMeansDomain] =
    InlandModePage.reader.flatMap {
      case InlandMode.Mail =>
        UserAnswersReader(TransportMeansDomainWithMailInlandMode).widen[TransportMeansDomain]
      case x =>
        UserAnswersReader[TransportMeansDomainWithOtherInlandMode](
          TransportMeansDomainWithOtherInlandMode.userAnswersReader(x)
        ).widen[TransportMeansDomain]
    }
}

case object TransportMeansDomainWithMailInlandMode extends TransportMeansDomain {
  override val inlandMode: InlandMode = InlandMode.Mail
}

case class TransportMeansDomainWithOtherInlandMode(
  override val inlandMode: InlandMode,
  transportMeansDeparture: TransportMeansDepartureDomain,
  borderModeOfTransport: Option[BorderModeOfTransport],
  transportMeansActiveList: TransportMeansActiveListDomain
) extends TransportMeansDomain

object TransportMeansDomainWithOtherInlandMode {

  implicit def userAnswersReader(inlandMode: InlandMode): UserAnswersReader[TransportMeansDomainWithOtherInlandMode] = {

    implicit val borderModeOfTransportReads: UserAnswersReader[Option[BorderModeOfTransport]] =
      SecurityDetailsTypePage.reader.flatMap {
        case NoSecurityDetails => BorderModeOfTransportPage.reader.map(Some(_))
        case _                 => none[BorderModeOfTransport].pure[UserAnswersReader]
      }

    (
      UserAnswersReader(inlandMode),
      UserAnswersReader[TransportMeansDepartureDomain],
      borderModeOfTransportReads,
      UserAnswersReader[TransportMeansActiveListDomain]
    ).tupled.map((TransportMeansDomainWithOtherInlandMode.apply _).tupled)
  }
}
