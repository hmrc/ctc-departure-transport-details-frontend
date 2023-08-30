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

package models

import config.Constants._

sealed trait Ternary extends Radioable[Ternary] {

  override val messageKeyPrefix: String = Ternary.messageKeyPrefix
}

object Ternary extends EnumerableType[Ternary] {

  case object True extends WithName("true") with Ternary
  case object False extends WithName("false") with Ternary
  case object Maybe extends WithName("maybe") with Ternary

  val messageKeyPrefix: String = "preRequisites.containerIndicator"

  override val values: Seq[Ternary] = Seq(True, False, Maybe)

  def values(additionalDeclarationType: String): Seq[Ternary] =
    additionalDeclarationType match {
      case STANDARD =>
        values.filterNot(_ == Maybe)
      case _ =>
        values
    }
}
