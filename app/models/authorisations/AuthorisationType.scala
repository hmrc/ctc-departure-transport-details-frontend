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

import models.ProcedureType.Simplified
import models.domain.GettableAsReaderOps
import models.transportMeans.departure.InlandMode.{Air, Maritime, Rail}
import models.{EnumerableType, Index, Radioable, UserAnswers, WithName}
import pages.external.{ApprovedOperatorPage, ProcedureTypePage}
import pages.transportMeans.departure.InlandModePage
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

  def values(userAnswers: UserAnswers): Seq[AuthorisationType] = {
    val reader = for {
      procedureType           <- ProcedureTypePage.reader
      reducedDataSetIndicator <- ApprovedOperatorPage.inferredReader
      inlandMode              <- InlandModePage.reader
    } yield (reducedDataSetIndicator, inlandMode, procedureType) match {
      case (true, Maritime | Rail | Air, _) => Seq(TRD)
      case (true, _, Simplified)            => Seq(ACR)
      case _                                => values
    }
    reader.run(userAnswers).getOrElse(values)
  }

  def values(userAnswers: UserAnswers, index: Index): Seq[AuthorisationType] =
    if (index.isFirst) values(userAnswers) else values

}
