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

package models.authorisations

import models._
import models.ProcedureType.Simplified
import models.domain.{GettableAsReaderOps, JsArrayGettableAsReaderOps}
import models.transportMeans.InlandMode._
import models.{EnumerableType, Index, Radioable, UserAnswers, WithName}
import pages.authorisationsAndLimit.authorisations.index.InferredAuthorisationTypePage
import pages.external.{ApprovedOperatorPage, ProcedureTypePage}
import pages.sections.authorisationsAndLimit.AuthorisationsSection
import pages.transportMeans.InlandModePage
import play.api.i18n.Messages

sealed trait AuthorisationType extends Radioable[AuthorisationType] {

  override val messageKeyPrefix: String = AuthorisationType.messageKeyPrefix

  def asString(implicit messages: Messages): String =
    messages(s"${AuthorisationType.messageKeyPrefix}.$this")

  def forDisplay(implicit messages: Messages): String =
    messages(s"${AuthorisationType.messageKeyPrefix}.forDisplay.$this")

}

object AuthorisationType extends EnumerableType[AuthorisationType] {

  case object ACR extends WithName("ACR") with AuthorisationType
  case object SSE extends WithName("SSE") with AuthorisationType
  case object TRD extends WithName("TRD") with AuthorisationType

  val messageKeyPrefix: String = "authorisations.authorisationType"

  val values: Seq[AuthorisationType] = Seq(
    ACR,
    SSE,
    TRD
  )

  // TODO we can potentially remove this method
  def values(userAnswers: UserAnswers): Seq[AuthorisationType] = {
    val reader = for {
      procedureType           <- ProcedureTypePage.reader
      reducedDataSetIndicator <- ApprovedOperatorPage.inferredReader
      inlandMode              <- InlandModePage.reader
    } yield (reducedDataSetIndicator, inlandMode, procedureType) match {
      case (true, Maritime | Rail | Air, _)                   => Seq(TRD)
      case (true, Road | Fixed | Mail | Waterway, Simplified) => Seq(ACR)
      case (false, Maritime | Rail | Air, Simplified)         => Seq(ACR)
      case _                                                  => values
    }
    reader.run(userAnswers).getOrElse(values)
  }

  // TODO Update this to return only types that have not been selected excluding the current index
  def values(userAnswers: UserAnswers, index: Index): Seq[AuthorisationType] = {

    val existingValues = AuthorisationsSection.optionalReader.flatMap {
      case Some(array) if array.nonEmpty => ???
      case _                             => ???
    }

    if (index.isFirst) values(userAnswers) else values
  }
}
