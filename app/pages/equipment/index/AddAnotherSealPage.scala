/*
 * Copyright 2025 HM Revenue & Customs
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

package pages.equipment.index

import controllers.equipment.index.routes
import models.{Index, Mode, UserAnswers}
import pages.AddAnotherPage
import pages.sections.equipment.EquipmentSection
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class AddAnotherSealPage(equipmentIndex: Index) extends AddAnotherPage {

  override def path: JsPath = EquipmentSection(equipmentIndex).path \ toString

  override def toString: String = "addAnotherSeal"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.AddAnotherSealController.onPageLoad(userAnswers.lrn, mode, equipmentIndex))
}
