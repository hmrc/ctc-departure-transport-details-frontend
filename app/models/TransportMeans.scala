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

import cats.conversions.all.autoConvertProfunctorVariance
import models.reference.transportMeans.departure.Identification
import pages.transportMeans.departure.{IdentificationPage, MeansIdentificationNumberPage}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads

case class TransportMeans(index: Index, identificationType: Option[Identification], identificationNumber: Option[String]) {

  def forRemoveDisplay: String = (identificationType, identificationNumber) match {
    case (Some(a), Some(b)) => s"$a - $b"
    case (Some(a), None)    => a.toString
    case (None, Some(b))    => b
    case (None, None)       => ""
  }

}

object TransportMeans {

  def apply(userAnswers: UserAnswers, transportMeansIndex: Index): Option[TransportMeans] = {
    implicit val reads: Reads[TransportMeans] = (
      IdentificationPage(transportMeansIndex).path.readNullable[Identification] and
        MeansIdentificationNumberPage(transportMeansIndex).path.readNullable[String]
    ).apply {
      (identifier, identificationNumber) => TransportMeans(transportMeansIndex, identifier, identificationNumber)
    }
    userAnswers.data.asOpt[TransportMeans]
  }
}
