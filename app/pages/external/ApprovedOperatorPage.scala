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

package pages.external

import pages.ReadOnlyPage
import models.DeclarationType.Option4
import play.api.libs.json.JsPath
import models.domain.{GettableAsReaderOps, UserAnswersReader}

case object ApprovedOperatorPage extends ReadOnlyPage[Boolean] {

  override def path: JsPath = traderDetailsConsignmentPath \ toString

  override def toString: String = "approvedOperator"

  def inferredReader: UserAnswersReader[Boolean] = DeclarationTypePage.reader.flatMap {
    case Option4 => UserAnswersReader(false)
    case _       => ApprovedOperatorPage.reader
  }
}
