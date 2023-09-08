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
import pages.authorisationsAndLimit.authorisations.index.{AuthorisationTypePage, InferredAuthorisationTypePage}
import pages.sections.authorisationsAndLimit.AuthorisationsSection
import play.api.i18n.Messages

sealed trait AuthorisationType extends Radioable[AuthorisationType] {

  override val messageKeyPrefix: String = AuthorisationType.messageKeyPrefix

  def forDisplay(implicit messages: Messages): String =
    messages(s"${AuthorisationType.messageKeyPrefix}.forDisplay.$this")

}

object AuthorisationType extends EnumerableType[AuthorisationType] {

  case object ACR extends WithName("ACR") with AuthorisationType {
    override val code: String = ""
  }

  case object SSE extends WithName("SSE") with AuthorisationType {
    override val code: String = ""
  }

  case object TRD extends WithName("TRD") with AuthorisationType {
    override val code: String = ""
  }

  val messageKeyPrefix: String = "authorisations.authorisationType"

  val values: Seq[AuthorisationType] = Seq(
    ACR,
    SSE,
    TRD
  )

  def values(userAnswers: UserAnswers, index: Index): Seq[AuthorisationType] = {
    val numberOfAuthorisations = userAnswers.getArraySize(AuthorisationsSection)
    val authorisationTypes = (0 until numberOfAuthorisations).map(Index(_)).foldLeft[Seq[AuthorisationType]](Nil) {
      (acc, authorisationIndex) =>
        userAnswers.get(AuthorisationTypePage(authorisationIndex)) orElse userAnswers.get(InferredAuthorisationTypePage(authorisationIndex)) match {
          case Some(value) if authorisationIndex != index => acc :+ value
          case _                                          => acc
        }
    }

    values.diff(authorisationTypes)
  }
}
