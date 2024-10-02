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

package models.removable

import models.reference.transportMeans.active.Identification
import models.{Index, UserAnswers}
import pages.transportMeans.active._
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Reads

case class ActiveBorderTransport(identification: Identification, identificationNumber: Option[String]) {

  def forRemoveDisplay: String = identificationNumber match {
    case Some(value) => s"$identification - $value"
    case None        => identification.toString
  }

}

object ActiveBorderTransport {

  def apply(userAnswers: UserAnswers, activeIndex: Index): Option[ActiveBorderTransport] = {
    implicit val reads: Reads[ActiveBorderTransport] = (
      IdentificationPage(activeIndex).path.read[Identification] and
        IdentificationNumberPage(activeIndex).path.readNullable[String]
    ).apply {
      (identification, identificationNumber) => ActiveBorderTransport(identification, identificationNumber)
    }
    userAnswers.data.asOpt[ActiveBorderTransport]
  }

}
