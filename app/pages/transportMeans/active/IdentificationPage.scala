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

package pages.transportMeans.active

import controllers.transportMeans.active.routes
import models.domain.{GettableAsReaderOps, UserAnswersReader}
import models.transportMeans.BorderModeOfTransport
import models.transportMeans.active.Identification
import models.transportMeans.active.Identification.{RegNumberRoadVehicle, TrainNumber}
import models.{Index, Mode, UserAnswers}
import pages.QuestionPage
import pages.sections.transportMeans.TransportMeansActiveSection
import pages.transportMeans.BorderModeOfTransportPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case class IdentificationPage(index: Index) extends QuestionPage[Identification] {

  override def path: JsPath = TransportMeansActiveSection(index).path \ toString

  override def toString: String = "identification"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.IdentificationController.onPageLoad(userAnswers.lrn, mode, index))

  override def cleanup(value: Option[Identification], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(_) => userAnswers.remove(IdentificationNumberPage(index))
      case _       => super.cleanup(value, userAnswers)
    }

  def inferredReader: UserAnswersReader[Identification] =
    if (index.isFirst) {
      BorderModeOfTransportPage.reader.flatMap {
        case BorderModeOfTransport.ChannelTunnel     => UserAnswersReader.apply(TrainNumber)
        case BorderModeOfTransport.IrishLandBoundary => UserAnswersReader.apply(RegNumberRoadVehicle)
        case _                                       => IdentificationPage(index).reader
      }
    } else {
      IdentificationPage(index).reader
    }
}
