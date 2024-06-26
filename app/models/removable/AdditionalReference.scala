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

import models.reference.additionalReference.AdditionalReferenceType
import models.{Index, UserAnswers}
import pages.additionalReference.index.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Reads

case class AdditionalReference(`type`: AdditionalReferenceType, referenceNumber: Option[String]) {

  def forRemoveDisplay: String = referenceNumber match {
    case Some(value) => s"${`type`} - $value"
    case None        => `type`.toString
  }

}

object AdditionalReference {

  def apply(userAnswers: UserAnswers, additionalReferenceIndex: Index): Option[AdditionalReference] = {
    implicit val reads: Reads[AdditionalReference] = (
      AdditionalReferenceTypePage(additionalReferenceIndex).path.read[AdditionalReferenceType] and
        AdditionalReferenceNumberPage(additionalReferenceIndex).path.readNullable[String]
    ).apply {
      (additionalReferenceType, referenceNumber) => AdditionalReference(additionalReferenceType, referenceNumber)
    }
    userAnswers.data.asOpt[AdditionalReference]
  }

}
