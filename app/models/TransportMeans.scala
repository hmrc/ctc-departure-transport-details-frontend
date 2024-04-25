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

package models

import models.reference.transportMeans.departure.Identification
import pages.transportMeans.departure.{IdentificationPage, MeansIdentificationNumberPage}
import play.api.i18n.Messages
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads

case class TransportMeans(index: Index, identificationType: Option[Identification], identificationNumber: Option[String]) {

  def forRemoveDisplay: String = (identificationType, identificationNumber) match {
    case (Some(a), Some(b)) => s"$a - $b"
    case (Some(a), None)    => a.toString
    case (None, Some(b))    => b
    case (None, None)       => ""
  }

  def forAddAnotherDisplay(implicit messages: Messages): Option[String] = (identificationType, identificationNumber) match {
    case (Some(identification), Some(identificationNumber)) =>
      Some(messages("departureTransportMeans.label.bothArgs", index.display, identification.asString, identificationNumber))
    case (Some(identification), None)       => Some(messages("departureTransportMeans.label.oneArg", index.display, identification.asString))
    case (None, Some(identificationNumber)) => Some(messages("departureTransportMeans.label.oneArg", index.display, identificationNumber))
    case _                                  => Some(messages("departureTransportMeans.label.noArgs", index.display))
  }
}

object TransportMeans {

  def apply(userAnswers: UserAnswers, departureIndex: Index): Option[TransportMeans] = {
    implicit val reads: Reads[TransportMeans] = (
      IdentificationPage(departureIndex).path.readNullable[Identification] and
        MeansIdentificationNumberPage(departureIndex).path.readNullable[String]
    ).apply {
      (identifier, identificationNumber) => TransportMeans(departureIndex, identifier, identificationNumber)
    }
    userAnswers.data.asOpt[TransportMeans]
  }
}
